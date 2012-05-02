package org.platformlayer.service.cloud.direct.ops;

import org.platformlayer.ops.BindingScope;
import org.platformlayer.ops.CustomRecursor;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpaqueMachine;
import org.platformlayer.ops.OperationRecursor;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.SshKey;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.tree.OpsTreeBase;

public class DirectTarget extends OpsTreeBase implements CustomRecursor {

	public NetworkPoint address;
	public SshKey sshKey;

	@Override
	protected void addChildren() {

	}

	@Handler
	public void handler() {
	}

	@Override
	public void doRecurseOperation() throws OpsException {
		Machine machine = new OpaqueMachine(address);
		OpsTarget target = machine.getTarget(sshKey);

		BindingScope scope = BindingScope.push(machine, target);
		try {
			OpsContext opsContext = OpsContext.get();
			OperationRecursor.doRecurseChildren(opsContext, this);
		} finally {
			scope.pop();
		}
	}

}
