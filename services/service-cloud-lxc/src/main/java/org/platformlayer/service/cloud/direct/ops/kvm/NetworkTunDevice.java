package org.platformlayer.service.cloud.direct.ops.kvm;

import javax.inject.Provider;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.process.ProcessExecution;
import org.platformlayer.ops.process.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkTunDevice {

	private static final Logger log = LoggerFactory.getLogger(NetworkTunDevice.class);

	public String interfaceName;
	public Provider<String> bridgeName;

	@Handler
	public void handler(OpsTarget target) throws OpsException {
		if (OpsContext.isConfigure()) {
			Command findCommand = Command.build("/sbin/ifconfig {0}", interfaceName);
			boolean found = false;
			try {
				target.executeCommand(findCommand);
				found = true;
			} catch (ProcessExecutionException e) {
				ProcessExecution execution = e.getExecution();
				if (execution.getExitCode() == 1 && execution.getStdErr().contains("Device not found")) {
					found = false;
				} else {
					throw new OpsException("Error checking for interface", e);
				}
			}

			if (!found) {
				// This is actually idempotent, but I think it's scary to rely on it being so
				Command command = Command.build("/usr/sbin/tunctl -t {0}", interfaceName);
				target.executeCommand(command);
			}

			{
				// TODO: Safe to re-run?
				Command command = Command.build("ifconfig {0} up", interfaceName);
				target.executeCommand(command);
			}

			if (bridgeName != null) {
				// TODO: Safe to re-run?

				Command command = Command.build("brctl addif {0} {1}", bridgeName.get(), interfaceName);
				try {
					target.executeCommand(command);
				} catch (ProcessExecutionException e) {
					ProcessExecution execution = e.getExecution();
					if (execution.getExitCode() == 1 && execution.getStdErr().contains("already a member of a bridge")) {
						// OK
						// TODO: Check that the bridge is bridgeName
					} else {
						throw new OpsException("Error attaching interface to bridge", e);
					}
				}
			}
		}

		if (OpsContext.isDelete()) {
			// TODO: Implement
			log.warn("NetworkTunDevice delete not implemented");
		}
	}
}
