package org.platformlayer.guice.xaas;

import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.xml.bind.JAXBException;

import org.platformlayer.RepositoryException;
import org.platformlayer.common.JobState;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ids.ItemType;
import org.platformlayer.ids.ManagedItemId;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.jdbc.DbHelperBase;
import org.platformlayer.jdbc.JdbcConnection;
import org.platformlayer.jdbc.JdbcTransaction;
import org.platformlayer.jdbc.proxy.Query;
import org.platformlayer.jdbc.proxy.QueryFactory;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.xaas.repository.JobRepository;
import org.platformlayer.xaas.services.ServiceProviderDictionary;
import org.platformlayer.xml.JaxbHelper;

public class JdbcJobRepository implements JobRepository {
	@Inject
	ServiceProviderDictionary serviceProviderDictionary;

	@Inject
	Provider<JdbcConnection> connectionProvider;

	@Override
	public JobData getJob(PlatformLayerKey jobId, boolean fetchLog) {
		throw new UnsupportedOperationException();
	}

	@Override
	@JdbcTransaction
	public void recordJob(PlatformLayerKey jobId, PlatformLayerKey itemKey, JobState jobState, JobLog jobLog)
			throws RepositoryException {
		DbHelper db = new DbHelper();
		try {
			String data;

			// TODO: More compact encoding?? XML InfoSet? GZIP?
			try {
				data = JaxbHelper.toXml(jobLog, false);
			} catch (JAXBException e) {
				throw new RepositoryException("Error serializing job log", e);
			}

			int updateCount = db.insertJobLog(itemKey.getServiceType(), itemKey.getItemType(), itemKey.getProject(),
					itemKey.getItemId(), jobState, data);

			if (updateCount != 1) {
				throw new IllegalStateException("Unexpected number of rows inserted");
			}
		} catch (SQLException e) {
			throw new RepositoryException("Error saving job log", e);
		} finally {
			db.close();
		}
	}

	static interface Queries {
		@Query("INSERT INTO job_logs (service, model, account, item, jobstate, data) VALUES (?, ?, ?, ?, ?, ?)")
		int insertJobLog(int service, int model, int project, String key, int jobState, String data)
				throws SQLException;
	}

	@Inject
	QueryFactory queryFactory;

	class DbHelper extends DbHelperBase {
		final Queries queries;

		public DbHelper() {
			super(connectionProvider.get().getConnection());

			this.queries = queryFactory.get(Queries.class);
		}

		public int insertJobLog(ServiceType serviceType, ItemType itemType, ProjectId project, ManagedItemId itemId,
				JobState jobState, String data) throws SQLException {
			return queries.insertJobLog(mapToValue(serviceType), mapToValue(itemType), mapToValue(project),
					itemId.getKey(), jobState.getCode(), data);
		}
	}
}
