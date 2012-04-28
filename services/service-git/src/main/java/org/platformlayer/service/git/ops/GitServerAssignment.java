package org.platformlayer.service.git.ops;

import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.ops.CustomRecursor;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.helpers.SshKey;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.git.model.GitRepository;
import org.platformlayer.service.git.model.GitService;

public class GitServerAssignment extends OpsTreeBase implements CustomRecursor {
	static final Logger log = Logger.getLogger(GitServerAssignment.class);

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	InstanceHelpers instances;

	@Inject
	ServiceContext service;

	@Handler
	public void handler(GitRepository model) throws Exception {
		// TODO: Support backup on that GitServer
		List<GitService> gitServices = platformLayer.listItems(GitService.class);

		GitService gitService = null;
		if (gitServices.size() == 0) {
			throw new OpsException("No git servers found");
		} else {
			// TODO: Assign to a single git server
			if (gitServices.size() != 1) {
				throw new OpsException("Only 1 git server implemented at the moment");
			}
			gitService = gitServices.get(0);
		}

		if (OpsContext.isDelete()) {
			if (gitService == null) {
				log.info("Deleting, but not assigned to a server; nothing to do");
				getRecursionState().setPreventRecursion(true);
				return;
			}
		}

		if (gitService.getState() != ManagedItemState.ACTIVE) {
			throw new OpsException("Server not yet active: " + gitService);
		}

		Machine machine = instances.findMachine(gitService);
		if (machine == null) {
			throw new OpsException("Server machine not found:" + gitService);
		}

		SshKey sshKey = service.getSshKey();
		OpsTarget target = machine.getTarget(sshKey);

		getRecursionState().pushChildScope(OpsTarget.class, target);
	}

	@Override
	protected void addChildren() throws OpsException {

	}

}
