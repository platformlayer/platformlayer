package org.platformlayer.ops.supervisor;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

public class SupervisorInstance {
	public String id;

	@Handler
	public void handler(OpsTarget target) throws OpsException {
		if (OpsContext.isConfigure()) {
			target.executeCommand(Command.build("/usr/bin/supervisorctl update {0}", id));

			target.executeCommand(Command.build("/usr/bin/supervisorctl start {0}", id));
		}
	}
}
