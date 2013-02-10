package org.platformlayer.ops.jobstore.jdbc;

import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.platformlayer.RepositoryException;
import org.platformlayer.TimeSpan;
import org.platformlayer.core.model.Action;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ids.ManagedItemId;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.jdbc.DbHelperBase;
import org.platformlayer.jdbc.JdbcConnection;
import org.platformlayer.jdbc.JdbcTransaction;
import org.platformlayer.jdbc.proxy.Query;
import org.platformlayer.jdbc.proxy.QueryFactory;
import org.platformlayer.jdbc.simplejpa.JoinedQueryResult;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobExecutionData;
import org.platformlayer.jobs.model.JobState;
import org.platformlayer.xaas.repository.JobRepository;
import org.platformlayer.xaas.services.ServiceProviderDictionary;

import com.fathomdb.Casts;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class JdbcJobRepository implements JobRepository {
	@Inject
	ServiceProviderDictionary serviceProviderDictionary;

	@Inject
	Provider<JdbcConnection> connectionProvider;

	static interface Queries {
		// @Query("INSERT INTO jobs (service, model, account, item, jobstate, data) VALUES (?, ?, ?, ?, ?, ?)")
		// int insertJobLog(int service, int model, int project, String key, int jobState, String data)
		// throws SQLException;

		@Query("SELECT * FROM job_execution WHERE project=? and job_id=?")
		List<JobExecutionEntity> listExecutions(int projectId, String jobId) throws SQLException;

		@Query("SELECT * FROM job_execution WHERE project=? and started_at >= (current_timestamp - (? * interval '1 second'))")
		List<JobExecutionEntity> listRecentExecutions(int projectId, long totalSeconds) throws SQLException;

		@Query("SELECT * FROM job WHERE project=? and id IN (SELECT job_id FROM job_execution WHERE project=? and started_at >= (current_timestamp - (? * interval '1 second')))")
		List<JobEntity> listRecentJobs(int projectId, int projectIdDup, long totalSeconds);

		@Query("SELECT * FROM job j, job_execution je WHERE je.project=? and je.started_at >= (current_timestamp - (? * interval '1 second')) AND (j.project = je.project) and (j.id = je.job_id)")
		JoinedQueryResult listRecentJobsAndExecutions(int projectId, long totalSeconds);

		@Query("SELECT * FROM job WHERE project=? and id=?")
		JobEntity findJob(int projectId, String jobId) throws SQLException;

		@Query("SELECT * FROM job_execution WHERE project=? and job_id=? and id=?")
		JobExecutionEntity findExecution(int projectId, String jobId, String executionId) throws SQLException;

		@Query("UPDATE job_execution SET ended_at=?, state=? WHERE project=? and job_id=? and id=?")
		int updateExecution(Date endDate, JobState state, int projectId, String jobId, String executionId)
				throws SQLException;

		@Query(value = Query.AUTOMATIC_INSERT)
		int insert(JobExecutionEntity entity) throws SQLException;

		@Query(value = Query.AUTOMATIC_INSERT)
		int insert(JobEntity entity) throws SQLException;

	}

	@Inject
	QueryFactory queryFactory;

	@Inject
	JAXBContext jaxbContext;

	class DbHelper extends DbHelperBase {
		final Queries queries;

		public DbHelper() {
			super(connectionProvider.get());

			this.queries = queryFactory.get(Queries.class);
		}

		// public int insertJobLog(ServiceType serviceType, ItemType itemType, ProjectId project, ManagedItemId itemId,
		// JobState jobState, String data) throws SQLException {
		// return queries.insertJobLog(mapToValue(serviceType), mapToValue(itemType), mapToValue(project),
		// itemId.getKey(), jobState.getCode(), data);
		// }
	}

	// @Override
	// @JdbcTransaction
	// public void recordJob(PlatformLayerKey jobId, PlatformLayerKey itemKey, JobState jobState, JobLog jobLog)
	// throws RepositoryException {
	// DbHelper db = new DbHelper();
	// try {
	// String data;
	//
	// // TODO: More compact encoding?? XML InfoSet? GZIP?
	// try {
	// data = JaxbHelper.toXml(jobLog, false);
	// } catch (JAXBException e) {
	// throw new RepositoryException("Error serializing job log", e);
	// }
	//
	// int updateCount = db.insertJobLog(itemKey.getServiceType(), itemKey.getItemType(), itemKey.getProject(),
	// itemKey.getItemId(), jobState, data);
	//
	// if (updateCount != 1) {
	// throw new IllegalStateException("Unexpected number of rows inserted");
	// }
	// } catch (SQLException e) {
	// throw new RepositoryException("Error saving job log", e);
	// } finally {
	// db.close();
	// }
	// }

	@Override
	@JdbcTransaction
	public List<JobExecutionData> listExecutions(PlatformLayerKey jobKey) throws RepositoryException {
		DbHelper db = new DbHelper();
		try {
			ProjectId projectId = jobKey.getProject();
			String jobId = jobKey.getItemIdString();

			List<JobExecutionEntity> executions = db.queries.listExecutions(db.mapToValue(projectId), jobId);
			List<JobExecutionData> ret = Lists.newArrayList();
			for (JobExecutionEntity execution : executions) {
				ret.add(mapFromEntity(execution, jobKey));
			}
			return ret;
		} catch (SQLException e) {
			throw new RepositoryException("Error listing job executions", e);
		} finally {
			db.close();
		}
	}

	@Override
	@JdbcTransaction
	public List<JobExecutionData> listRecentExecutions(ProjectId projectId, TimeSpan window) throws RepositoryException {
		DbHelper db = new DbHelper();
		try {
			// We use JoinedQueryResult because we have a compound PK (projectId / jobId)
			// and JPA makes this really complicated.

			// TODO: Is it really a compound PK? Should jobId be globally unique?
			JoinedQueryResult results = db.queries.listRecentJobsAndExecutions(db.mapToValue(projectId),
					window.getTotalSeconds());

			List<JobExecutionData> ret = Lists.newArrayList();
			Map<String, JobData> jobs = Maps.newHashMap();

			for (JobEntity job : results.getAll(JobEntity.class)) {
				ManagedItemId jobId = new ManagedItemId(job.jobId);
				PlatformLayerKey jobKey = JobData.buildKey(projectId, jobId);
				jobs.put(job.jobId, mapFromEntity(job, jobKey));
			}

			for (JobExecutionEntity execution : results.getAll(JobExecutionEntity.class)) {
				JobData jobData = jobs.get(execution.jobId);
				if (jobData == null) {
					throw new IllegalStateException();
				}

				ManagedItemId jobId = new ManagedItemId(execution.jobId);
				PlatformLayerKey jobKey = JobData.buildKey(projectId, jobId);
				JobExecutionData run = mapFromEntity(execution, jobKey);
				run.job = jobData;
				ret.add(run);
			}

			return ret;
		} catch (SQLException e) {
			throw new RepositoryException("Error listing job executions", e);
		} finally {
			db.close();
		}
	}

	@Override
	@JdbcTransaction
	public List<JobData> listRecentJobs(ProjectId projectId, TimeSpan window) throws RepositoryException {
		DbHelper db = new DbHelper();
		try {
			int project = db.mapToValue(projectId);
			List<JobEntity> jobs = db.queries.listRecentJobs(project, project, window.getTotalSeconds());
			List<JobData> ret = Lists.newArrayList();
			for (JobEntity job : jobs) {
				ManagedItemId jobId = new ManagedItemId(job.jobId);
				PlatformLayerKey jobKey = JobData.buildKey(projectId, jobId);
				ret.add(mapFromEntity(job, jobKey));
			}
			return ret;
		} catch (SQLException e) {
			throw new RepositoryException("Error listing job executions", e);
		} finally {
			db.close();
		}
	}

	@Override
	@JdbcTransaction
	public JobExecutionData findExecution(PlatformLayerKey jobKey, String executionId) throws RepositoryException {
		DbHelper db = new DbHelper();
		try {
			ProjectId projectId = jobKey.getProject();
			String jobId = jobKey.getItemIdString();

			JobExecutionEntity execution = db.queries.findExecution(db.mapToValue(projectId), jobId, executionId);

			if (execution == null) {
				return null;
			}

			return mapFromEntity(execution, jobKey);
		} catch (SQLException e) {
			throw new RepositoryException("Error listing job executions", e);
		} finally {
			db.close();
		}
	}

	private JobExecutionData mapFromEntity(JobExecutionEntity execution, PlatformLayerKey jobKey) {
		JobExecutionData data = new JobExecutionData();
		data.executionId = execution.executionId;
		data.endedAt = execution.endedAt;
		data.startedAt = execution.startedAt;
		data.state = execution.state;
		data.executionId = execution.executionId;
		data.jobKey = jobKey;
		return data;
	}

	private JobData mapFromEntity(JobEntity entity, PlatformLayerKey jobKey) throws RepositoryException {
		JobData data = new JobData();

		data.action = actionFromXml(entity.actionXml);
		data.key = jobKey;
		data.targetId = PlatformLayerKey.parse(entity.target);

		return data;
	}

	private Action actionFromXml(String actionXml) throws RepositoryException {
		Object o;
		try {
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			o = unmarshaller.unmarshal(new StringReader(actionXml));
		} catch (JAXBException e) {
			throw new RepositoryException("Error deserializing action", e);
		}

		return Casts.checkedCast(o, Action.class);
	}

	private String toXml(Action action) throws RepositoryException {
		Object o;
		try {
			Marshaller marshaller = jaxbContext.createMarshaller();
			StringWriter writer = new StringWriter();
			marshaller.marshal(action, writer);
			return writer.toString();
		} catch (JAXBException e) {
			throw new RepositoryException("Error serializing action", e);
		}
	}

	@Override
	@JdbcTransaction
	public JobData findJob(PlatformLayerKey jobKey) throws RepositoryException {
		DbHelper db = new DbHelper();
		try {
			ProjectId projectId = jobKey.getProject();
			String jobId = jobKey.getItemIdString();

			JobEntity execution = db.queries.findJob(db.mapToValue(projectId), jobId);

			if (execution == null) {
				return null;
			}

			return mapFromEntity(execution, jobKey);
		} catch (SQLException e) {
			throw new RepositoryException("Error listing job executions", e);
		} finally {
			db.close();
		}
	}

	@Override
	@JdbcTransaction
	public void recordJobEnd(PlatformLayerKey jobKey, String executionId, Date endedAt, JobState state)
			throws RepositoryException {
		DbHelper db = new DbHelper();
		try {
			ProjectId projectId = jobKey.getProject();
			String jobId = jobKey.getItemIdString();

			int updateCount = db.queries.updateExecution(endedAt, state, db.mapToValue(projectId), jobId, executionId);

			if (updateCount != 1) {
				throw new RepositoryException("Unexpected number of rows updated");
			}
		} catch (SQLException e) {
			throw new RepositoryException("Error updating job execution", e);
		} finally {
			db.close();
		}
	}

	@Override
	@JdbcTransaction
	public String insertExecution(PlatformLayerKey jobKey, Date startedAt) throws RepositoryException {
		DbHelper db = new DbHelper();
		try {
			ProjectId projectId = jobKey.getProject();
			String jobId = jobKey.getItemIdString();

			JobExecutionEntity execution = new JobExecutionEntity();
			execution.project = db.mapToValue(projectId);
			execution.startedAt = startedAt;
			execution.state = JobState.RUNNING;
			execution.executionId = UUID.randomUUID().toString();
			execution.jobId = jobId;

			int updateCount = db.queries.insert(execution);
			if (updateCount != 1) {
				throw new RepositoryException("Unexpected number of rows inserted");
			}

			return execution.executionId;
		} catch (SQLException e) {
			throw new RepositoryException("Error inserting job execution", e);
		} finally {
			db.close();
		}
	}

	@Override
	@JdbcTransaction
	public String insertJob(ProjectId projectId, JobData jobData) throws RepositoryException {
		DbHelper db = new DbHelper();
		try {
			String jobId = UUID.randomUUID().toString();

			JobEntity entity = new JobEntity();
			entity.project = db.mapToValue(projectId);
			entity.jobId = jobId;
			entity.actionXml = toXml(jobData.action);
			entity.target = jobData.targetId.getUrl();

			int updateCount = db.queries.insert(entity);
			if (updateCount != 1) {
				throw new RepositoryException("Unexpected number of rows inserted");
			}

			return jobId;
		} catch (SQLException e) {
			throw new RepositoryException("Error inserting job execution", e);
		} finally {
			db.close();
		}

	}

}
