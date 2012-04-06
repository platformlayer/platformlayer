package org.platformlayer.ops.tasks;

import java.util.List;
import java.util.concurrent.Callable;

import javax.ws.rs.WebApplicationException;

import org.apache.log4j.Logger;
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
import org.platformlayer.ops.jobs.JobRecord;
import org.platformlayer.ops.jobs.JobRegistry;
import org.platformlayer.xaas.repository.ManagedItemRepository;
import org.platformlayer.xaas.services.ServiceProvider;

import com.google.common.collect.Lists;

public class OperationWorker implements Callable<Object> {
    static final Logger log = Logger.getLogger(OperationWorker.class);

    final OpsSystem opsSystem;

    final OperationType operationType;
    final ServiceType serviceType;
    final OpsAuthentication auth;
    final PlatformLayerKey key;

    private final PlatformLayerKey jobKey;

    public OperationWorker(OpsSystem opsSystem, OperationType operationType, ServiceType serviceType, OpsAuthentication auth, PlatformLayerKey itemKey, PlatformLayerKey jobKey) {
        if (operationType == null)
            throw new IllegalArgumentException();

        this.opsSystem = opsSystem;
        this.operationType = operationType;
        this.serviceType = serviceType;
        this.auth = auth;
        this.key = itemKey;
        this.jobKey = jobKey;
    }

    Object doOperation() throws OpsException {
        OpsContextBuilder opsContextBuilder = opsSystem.getInjector().getInstance(OpsContextBuilder.class);

        final OpsContext opsContext = opsContextBuilder.buildOpsContext(serviceType, auth, jobKey);
        final JobRecord jobRecord = opsContext.getJobRecord();

        final Class<?> javaClass = opsContextBuilder.getJavaClass(key);
        final ServiceProvider serviceProvider = opsSystem.getServiceProvider(key.getServiceType());

        try {
            return OpsContext.runInContext(opsContext, new CheckedCallable<Object, Exception>() {
                @Override
                public Object call() throws Exception {
                    jobRecord.data.setState(JobState.RUNNING);

                    ItemBase item;
                    ManagedItemRepository repository = opsSystem.getManagedItemRepository();
                    try {
                        boolean fetchTags = true;
                        item = repository.getManagedItem(key, fetchTags, SecretProvider.withProject(auth.getProject()));
                    } catch (RepositoryException e) {
                        throw new OpsException("Error reading item from repository", e);
                    }

                    if (item == null)
                        throw new WebApplicationException(404);

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
                        repository.changeState(key, ManagedItemState.ACTIVE);
                        item.state = ManagedItemState.ACTIVE;
                        break;

                    case Delete:
                        repository.changeState(key, ManagedItemState.DELETED);
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
                    jobRecord.data.setState(JobState.SUCCESS);
                    return null;
                }
            });
        } catch (Throwable e) {
            log.warn("Error running operation", e);
            log.warn("Job finished with FAILED");

            jobRecord.data.setState(JobState.FAILED);

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
            opsSystem.getOperationQueue().submit(retryTask, retry);

            return null;
        } finally {
            JobRegistry jobRegistry = opsSystem.getJobRegistry();

            try {
                jobRegistry.recordJobEnd(jobRecord);
            } catch (OpsException e) {
                log.error("Error recording job in registry", e);
            }
        }
    }

    @Override
    public Object call() throws OpsException {
        Scope scope = Scope.empty();
        scope.put(OpsAuthentication.class, this.auth);
        try {
            scope.push();
            return doOperation();
        } finally {
            scope.pop();
        }
    }

}
