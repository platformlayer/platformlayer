package org.platformlayer.ops;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javalang7.AutoCloseable;
import javalang7.Utils;

import org.apache.log4j.Logger;
import org.platformlayer.ApplicationMode;
import org.platformlayer.CheckedCallable;
import org.platformlayer.Scope;
import org.platformlayer.ops.log.JobLogger;
import org.platformlayer.ops.model.metrics.MetricConfig;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.tasks.JobRecord;
import org.platformlayer.ops.tasks.OperationInvoker;

import com.google.common.collect.Lists;
import com.google.inject.Injector;

public class OpsContext implements Closeable {
	static final Logger log = Logger.getLogger(OpsContext.class);

	final OpsSystem opsSystem;
	final UserInfo userInfo;
	final List<AutoCloseable> ownedObjects = Lists.newArrayList();

	final ServiceConfiguration serviceConfiguration;

	boolean isFailure = false;

	final JobLogger jobLogger;

	final JobRecord jobRecord;

	public OpsContext(OpsSystem opsSystem, JobRecord jobRecord, UserInfo userInfo,
			ServiceConfiguration serviceConfiguration) {
		super();
		this.opsSystem = opsSystem;
		this.jobRecord = jobRecord;
		this.userInfo = userInfo;
		this.serviceConfiguration = serviceConfiguration;
		this.jobLogger = new JobLogger(jobRecord.getLog());
	}

	public static OpsContext get() {
		Scope scope = Scope.get();
		if (scope == null) {
			return null;
		}
		return scope.get(OpsContext.class);
	}

	public <T> T getInstance(Class<T> clazz) {
		return BindingScope.get().getInstance(clazz);
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public static <T, E extends Exception> T runInContext(OpsContext opsContext, CheckedCallable<T, E> callable)
			throws OpsException {
		Scope scope = Scope.inherit();
		scope.put(opsContext);
		try {
			scope.push();
			return callable.call();
		} catch (Exception e) {
			log.warn("Error running operation", e);
			throw new OpsException("Error during operation", e);
		} finally {
			try {
				Utils.safeClose(opsContext);
			} catch (Exception e) {
				log.error("Error while closing OpsContext");
			}

			scope.pop();
		}
	}

	public OpsConfiguration getConfiguration() {
		return getOpsSystem().getConfiguration();
	}

	public OpsSystem getOpsSystem() {
		return opsSystem;
	}

	public void takeOwnership(AutoCloseable closeable) {
		ownedObjects.add(closeable);
	}

	@Override
	public void close() throws IOException {
		for (AutoCloseable closeable : ownedObjects) {
			Utils.safeClose(closeable);
		}
	}

	public OpaqueMachine connectToMachine(String dnsName) throws OpsException {
		return new OpaqueMachine(NetworkPoint.forPublicHostname(dnsName));
	}

	public ServiceConfiguration getServiceConfiguration() {
		return serviceConfiguration;
	}

	public void takeOwnership(final Machine machine) {
		takeOwnership(new AutoCloseable() {
			@Override
			public void close() throws Exception {
				if (isFailure() && ApplicationMode.isDevelopment()) {
					log.warn("FAILURE IN DEBUG MODE: will not shutdown machine");
					return;
				}
				machine.terminate();
			}
		});
	}

	public boolean isFailure() {
		return isFailure;
	}

	public void setFailure(boolean isFailure) {
		this.isFailure = isFailure;
	}

	public void addWarning(Object item, String format, Object... parameters) {
		// TODO: Implement addWarning
		log.warn("Stubbed-out: addWarning");
	}

	public void recurseOperation(Object target) throws OpsException {
		OperationInvoker operationInvoker = opsSystem.getInjector().getInstance(OperationInvoker.class);

		try {
			operationInvoker.invoke(target);
		} catch (IllegalArgumentException e) {
			throw new OpsException("Error invoking method on " + target.getClass(), e);
		} catch (IllegalAccessException e) {
			throw new OpsException("Error invoking method on " + target.getClass(), e);
		} catch (InvocationTargetException e) {
			throw new OpsException("Error invoking method on " + target.getClass(), e);
		}

		OperationRecursor.doRecurseOperation(this, target);
	}

	public MetricConfig getMetricInfo(Object target) throws OpsException {
		MetricCollector metricCollector = opsSystem.getInjector().getInstance(MetricCollector.class);
		return metricCollector.getMetricInfo(target);
	}

	public void recurseOperation(Object... targets) throws OpsException {
		for (Object target : targets) {
			recurseOperation(target);
		}
	}

	public void recurseOperation(Iterable<Object> targets) throws OpsException {
		for (Object target : targets) {
			recurseOperation(target);
		}
	}

	public void recurseOperation(BindingScope scope, Object... targets) throws OpsException {
		try {
			recurseOperation(targets);
		} finally {
			scope.pop();
		}
	}

	public Injector getInjector() {
		return opsSystem.getInjector();
	}

	public JobRecord getJobRecord() {
		return jobRecord;
	}

	public OperationType getOperationType() {
		return getInstance(OperationType.class);
	}

	public JobLogger getJobLogger() {
		return jobLogger;
	}

	public static boolean isDelete() {
		return isOperationType(OperationType.Delete);
	}

	public static boolean isOperationType(OperationType operationType) {
		OperationType thisOperationType = OpsContext.get().getOperationType();
		return thisOperationType == operationType;
	}

	public static boolean isConfigure() {
		return isOperationType(OperationType.Configure);
	}
}
