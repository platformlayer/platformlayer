package org.platformlayer.ops.supervisor;

import java.io.File;
import java.util.Map;

import javax.inject.Provider;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.tree.OpsTreeBase;

import com.google.inject.util.Providers;

public class StandardService extends OpsTreeBase {
	public String key;
	public Provider<Command> command;
	public File instanceDir;
	public String user;

	@Handler
	public void handler() {
	}

	public String getServiceId() {
		return key;
	}

	public void buildConfig(SupervisorProcessConfig config) throws OpsException {
		Map<String, String> properties = config.getProperties();
		properties.put("command", command.get().buildCommandString());

		if (instanceDir != null) {
			properties.put("directory", instanceDir.getAbsolutePath());
		}

		File logFile = getLogFile();
		if (logFile != null) {
			properties.put("redirect_stderr", "true");
			properties.put("stdout_logfile", logFile.getAbsolutePath());
			properties.put("stdout_logfile_maxbytes", "50MB");
			properties.put("stdout_logfile_backups", "0");
		}

		if (user != null) {
			properties.put("user", user);
		}
	}

	protected File getLogFile() {
		File logFile = new File("/var/log/" + key + ".log");
		return logFile;
	}

	@Override
	protected void addChildren() throws OpsException {
		ManagedSupervisorInstance instance = addChild(ManagedSupervisorInstance.class);
		instance.key = getServiceId();
		instance.config = new Provider<SupervisorProcessConfig>() {
			@Override
			public SupervisorProcessConfig get() {
				SupervisorProcessConfig config = new SupervisorProcessConfig(getServiceId());
				try {
					buildConfig(config);
				} catch (OpsException e) {
					throw new IllegalStateException("Error while building config", e);
				}
				return config;
			}

		};
	}

	public void setCommand(Command command) {
		this.command = Providers.of(command);
	}
}
