package org.platformlayer.ops.tasks;

import java.util.List;
import java.util.concurrent.Callable;

import javax.ws.rs.WebApplicationException;

import org.apache.log4j.Logger;
import org.openstack.utils.Io;
import org.platformlayer.ApplicationMode;
import org.platformlayer.CheckedCallable;
import org.platformlayer.RepositoryException;
import org.platformlayer.Scope;
import org.platformlayer.TimeSpan;
import org.platformlayer.auth.crypto.SecretProvider;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.exceptions.ExceptionHelpers;
import org.platformlayer.exceptions.HasRetryInfo;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.jobs.model.JobState;
import org.platformlayer.ops.BindingScope;
import org.platformlayer.ops.OperationType;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.auth.OpsAuthentication;
import org.platformlayer.ops.backups.BackupContext;
import org.platformlayer.xaas.repository.ManagedItemRepository;
import org.platformlayer.xaas.services.ServiceProvider;

import com.google.common.collect.Lists;

public class OperationWorker implements Callable<Object> {
	static final Logger log = Logger.getLogger(OperationWorker.class);

	final OpsSystem opsSystem;

	final JobRecord jobRecord;

	public OperationWorker(OpsSystem opsSystem, JobRecord jobRecord) {
		this.jobRecord = jobRecord;
		this.opsSystem = opsSystem;
	}

	Object doOperation() throws OpsException {
		final OperationType operationType = jobRecord.getOperationType();
		final PlatformLayerKey targetItemKey = jobRecord.getTargetItemKey();
		RenameThread rename = new RenameThread(operationType + " " + targetItemKey);
		try {
			OpsContextBuilder opsContextBuilder = opsSystem.getInjector().getInstance(OpsContextBuilder.class);

			final OpsAuthentication auth = jobRecord.getAuth();

			final OpsContext opsContext = opsContextBuilder.buildOpsContext(jobRecord);

			final ServiceType serviceType = jobRecord.getServiceType();
			final ServiceProvider serviceProvider = opsSystem.getServiceProvider(serviceType);

			try {
				return OpsContext.runInContext(opsContext, new CheckedCallable<Object, Exception>() {
					@Override
					public Object call() throws Exception {
						log.info("Starting job");
						jobRecord.setState(JobState.RUNNING, false);

						ItemBase item;
						ManagedItemRepository repository = opsSystem.getManagedItemRepository();
						try {
							boolean fetchTags = true;
							item = repository.getManagedItem(targetItemKey, fetchTags,
									SecretProvider.withProject(auth.getProject()));
						} catch (RepositoryException e) {
							throw new OpsException("Error reading item from repository", e);
						}

						if (item == null) {
							throw new WebApplicationException(404);
						}

						List<Object> scopeItems = Lists.newArrayList();

						switch (operationType) {
						case Configure:
							break;

						case Delete:
							break;

						case Backup: {
							BackupContext backupContext = BackupContext.build(item);
							scopeItems.add(backupContext);
						}
							break;

						default:
							throw new IllegalStateException();
						}

						Object controller = serviceProvider.getController(item.getClass());

						scopeItems.add(item);
						scopeItems.add(operationType);

						BindingScope scope = BindingScope.push(scopeItems);
						opsContext.recurseOperation(scope, controller);

						// TODO: Should we run a verify operation before -> ACTIVE??
						// (we need to fix the states as well)

						switch (operationType) {
						case Configure:
							repository.changeState(targetItemKey, ManagedItemState.ACTIVE);
							item.state = ManagedItemState.ACTIVE;
							break;

						case Delete:
							repository.changeState(targetItemKey, ManagedItemState.DELETED);
							item.state = ManagedItemState.DELETED;
							break;

						case Backup: {
							BackupContext backupContext = scope.getInstance(BackupContext.class);
							backupContext.writeDescriptor();
							break;
						}

						default:
							throw new IllegalStateException();
						}

						log.info("Job finished with SUCCESS");
						jobRecord.setState(JobState.SUCCESS, true);
						return null;
					}
				});
			} catch (Throwable e) {
				log.warn("Error running operation", e);
				log.warn("Job finished with FAILED");

				boolean isDone = false; // We will retry
				jobRecord.setState(JobState.FAILED, isDone);

				TimeSpan retry = null;

				HasRetryInfo retryInfo = ExceptionHelpers.findRetryInfo(e);
				if (retryInfo != null) {
					retry = retryInfo.getRetry();
				}

				if (retry == null) {
					// TODO: Eventually give up??
					retry = ApplicationMode.isDevelopment() ? TimeSpan.ONE_MINUTE : TimeSpan.FIVE_MINUTES;
				}

				// TODO: State transition??
				// managedItem.setState(ManagedItemState.ACTIVE, true);

				log.warn("Scheduling retry in " + retry);

				// TODO: Create new/copy of worker??
				OperationWorker retryTask = this;
				opsSystem.getOperationQueue().submitRetry(retryTask, retry);

				return null;
			} finally {
				JobRegistry jobRegistry = opsSystem.getJobRegistry();

				try {
					jobRegistry.recordJobEnd(jobRecord);
				} catch (OpsException e) {
					log.error("Error recording job in registry", e);
				}
			}
		} finally {
			Io.safeClose(rename);
		}
	}

	@Override
	public Object call() throws OpsException {
		Scope scope = Scope.empty();
		scope.put(OpsAuthentication.class, jobRecord.getAuth());
		try {
			scope.push();
			return doOperation();
		} finally {
			scope.pop();
		}
	}

}
