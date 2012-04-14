package org.platformlayer.service.cloud.direct.ops.kvm;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.process.ProcessExecutionException;

public class RunSupervisorInstance {
	public String id;

	@Handler
	public void handler(OpsTarget target) throws ProcessExecutionException {
		target.executeCommand(Command.build("supervisorctl update"));

		target.executeCommand(Command.build("supervisorctl start {0}", id));
	}
}
