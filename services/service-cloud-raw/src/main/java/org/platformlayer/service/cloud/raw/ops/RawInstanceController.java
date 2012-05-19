package org.platformlayer.service.cloud.raw.ops;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.raw.model.RawInstance;

public class RawInstanceController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(RawInstanceController.class);

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	ServiceContext service;

	@Inject
	InstanceHelpers instances;

	@Inject
	OpsSystem ops;

	@Handler
	public void handler(RawInstance rawMachine) throws OpsException, IOException {
		throw new OpsException("TODO: Remove utilization of INSTANCE_KEY");

		// String instanceKey = rawMachine.getTags().findUnique(Tag.INSTANCE_KEY);
		// if (instanceKey == null) {
		// RawTarget targetMachine = null;
		//
		// for (RawTarget candidate : platformLayer.listItems(RawTarget.class)) {
		// // TODO: Optimize this test
		// Tags tags = candidate.getTags();
		// String allocated = tags.findUnique(Tag.ASSIGNED);
		// if (allocated != null) {
		// continue;
		// }
		//
		// targetMachine = candidate;
		// break;
		// }
		//
		// if (targetMachine == null) {
		// throw new OpsException("Unable to allocate raw machine");
		// }
		//
		// platformLayer.addUniqueTag(OpsSystem.toKey(targetMachine),
		// Tag.buildTag(Tag.ASSIGNED, OpsSystem.toKey(rawMachine)));
		//
		// PlatformLayerKey serverId = OpsSystem.toKey(targetMachine);
		//
		// OpaqueMachine machine = new OpaqueMachine(NetworkPoint.forPublicHostname(targetMachine.host));
		// SshKey serviceSshKey = service.getSshKey();
		// OpsTarget target = machine.getTarget(serviceSshKey);
		//
		// PublicKey sshPublicKey = OpenSshUtils.readSshPublicKey(rawMachine.sshPublicKey);
		// SshAuthorizedKey.ensureSshAuthorization(target, "root", sshPublicKey);
		//
		// {
		// TagChanges tagChanges = new TagChanges();
		//
		// tagChanges.addTags.add(new Tag(Tag.INSTANCE_KEY, serverId.getUrl()));
		// tagChanges.addTags.add(new Tag(Tag.NETWORK_ADDRESS, targetMachine.host));
		//
		// platformLayer.changeTags(OpsSystem.toKey(rawMachine), tagChanges);
		// }
		// }
	}

	@Override
	protected void addChildren() throws OpsException {
	}

}
