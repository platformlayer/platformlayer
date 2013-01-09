package org.platformlayer.service.git.ops;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitRepositoryController extends OpsTreeBase {

	private static final Logger log = LoggerFactory.getLogger(GitRepositoryController.class);

	@Handler
	public void handler() throws Exception {
	}

	@Override
	protected void addChildren() throws OpsException {
		GitServerAssignment instance = injected(GitServerAssignment.class);
		addChild(instance);

		{
			GitRepositoryDirectory repo = injected(GitRepositoryDirectory.class);
			instance.addChild(repo);
		}
	}

}
