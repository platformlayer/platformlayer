package org.platformlayer.service.cloud.raw.ops;

import java.io.IOException;
import java.security.PublicKey;

import javax.inject.Inject;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpaqueMachine;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.helpers.SshKey;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.ssh.SshAuthorizedKey;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.raw.model.RawTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RawTargetController extends OpsTreeBase {

	private static final Logger log = LoggerFactory.getLogger(RawTargetController.class);

	@Inject
	ServiceContext service;

	@Handler
	public void handler(RawTarget rawTarget) throws OpsException, IOException {
		OpaqueMachine machine = new OpaqueMachine(NetworkPoint.forPublicHostname(rawTarget.host));
		SshKey serviceSshKey = service.getSshKey();
		OpsTarget target = machine.getTarget(serviceSshKey);

		// TODO: We have a bootstrapping problem here!!
		PublicKey sshPublicKey = service.getSshKey().getKeyPair().getPublic();
		SshAuthorizedKey.ensureSshAuthorization(target, "root", sshPublicKey);
	}

	@Override
	protected void addChildren() throws OpsException {
	}

}
