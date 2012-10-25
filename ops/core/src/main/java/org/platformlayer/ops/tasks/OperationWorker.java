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
import org.platformlayer.core.model.Action;
import org.platformlayer.core.model.BackupAction;
import org.platformlayer.core.model.ConfigureAction;
import org.platformlayer.core.model.DeleteAction;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.ValidateAction;
import org.platformlayer.exceptions.ExceptionHelpers;
import org.platformlayer.exceptions.HasRetryInfo;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.jobs.model.JobState;
import org.platformlayer.model.ProjectAuthorization;
import org.platformlayer.ops.BindingScope;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.backups.BackupContext;
import org.platformlayer.ops.backups.BackupContextFactory;
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
		final Action action = jobRecord.getAction();
		final PlatformLayerKey targetItemKey = jobRecord.getTargetItemKey();
		RenameThread rename = new RenameThread(action.getClass().getSimpleName() + " " + targetItemKey);
		try {
			OpsContextBuilder opsContextBuilder = opsSystem.getInjector().getInstance(OpsContextBuilder.class);

			final ProjectAuthorization project = jobRecord.getProjectAuthorization();

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
							item = repository.getManagedItem(targetItemKey, fetchTags, SecretProvider.from(project));
						} catch (RepositoryException e) {
							throw new OpsException("Error reading item from repository", e);
						}

						if (item == null) {
							throw new WebApplicationException(404);
						}

						List<Object> scopeItems = Lists.newArrayList();

						addActionScopeItems(action, item, scopeItems);

						Object controller = serviceProvider.getController(item);

						scopeItems.add(item);
						scopeItems.add(action);

						BindingScope scope = BindingScope.push(scopeItems);
						opsContext.recurseOperation(scope, controller);

						// TODO: Should we run a verify operation before -> ACTIVE??
						// (we need to fix the states as well)

						ManagedItemState newState = finishAction(action, scope);
						if (newState != null) {
							repository.changeState(targetItemKey, newState);
							item.state = newState;
						}

						log.info("Job finished with SUCCESS");
						jobRecord.setState(JobState.SUCCESS, true);
						return null;
					}

					private ManagedItemState finishAction(Action action, BindingScope scope) throws OpsException {
						ManagedItemState newState = null;
						if (action instanceof ConfigureAction) {
							newState = ManagedItemState.ACTIVE;
						}

						if (action instanceof ValidateAction) {
							// TODO: Change state to healthy??
						}

						if (action instanceof DeleteAction) {
							newState = ManagedItemState.DELETED;
						}

						if (action instanceof BackupAction) {
							BackupContext backupContext = scope.getInstance(BackupContext.class);
							backupContext.writeDescriptor();
						}

						return newState;
					}

					private void addActionScopeItems(Action action, ItemBase item, List<Object> scopeItems)
							throws OpsException {
						if (action instanceof BackupAction) {
							// TODO: Don't hard-code this
							BackupContextFactory backupContextFactory = opsSystem.getBackupContextFactory();
							BackupContext backupContext = backupContextFactory.build(item);
							scopeItems.add(backupContext);
						}
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
		scope.put(ProjectAuthorization.class, jobRecord.getProjectAuthorization());
		try {
			scope.push();
			return doOperation();
		} finally {
			scope.pop();
		}
	}

}
