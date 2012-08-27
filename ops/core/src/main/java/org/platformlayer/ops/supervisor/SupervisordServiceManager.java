package org.platformlayer.ops.supervisor;

import java.io.File;
import java.util.Map;

import javax.inject.Provider;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.tree.OpsTreeBase;

public class SupervisordServiceManager implements ServiceManager {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(SupervisordServiceManager.class);

	@Override
	public void addServiceInstance(final StandardService service) throws OpsException {
		ManagedSupervisordInstance instance = service.addChild(ManagedSupervisordInstance.class);
		instance.key = service.getServiceId();
		instance.config = new Provider<SupervisorProcessConfig>() {
			@Override
			public SupervisorProcessConfig get() {
				try {
					return buildConfig(service);
				} catch (OpsException e) {
					throw new IllegalStateException("Error while building config", e);
				}
			}
		};
	}

	static SupervisorProcessConfig buildConfig(StandardService service) throws OpsException {
		SupervisorProcessConfig config = new SupervisorProcessConfig(service.getServiceId());

		if (service.environment != null) {
			Map<String, String> env = config.getEnvironment();
			env.putAll(service.environment.get());
		}

		Map<String, String> properties = config.getProperties();
		properties.put("command", service.command.get().buildCommandString());

		if (service.instanceDir != null) {
			properties.put("directory", service.instanceDir.getAbsolutePath());
		}

		File logFile = service.getLogFile();
		if (logFile != null) {
			properties.put("redirect_stderr", "true");
			properties.put("stdout_logfile", logFile.getAbsolutePath());
			properties.put("stdout_logfile_maxbytes", "50MB");
			properties.put("stdout_logfile_backups", "0");
		}

		if (service.user != null) {
			properties.put("user", service.user);
		}

		return config;
	}

	@Override
	public void addServiceInstall(PlatformLayerKey owner, OpsTreeBase container) throws OpsException {
		container.addChild(SupervisordInstall.class);
	}

}
