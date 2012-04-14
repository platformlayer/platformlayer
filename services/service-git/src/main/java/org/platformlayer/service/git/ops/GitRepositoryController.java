package org.platformlayer.service.git.ops;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.tree.OpsTreeBase;

public class GitRepositoryController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(GitRepositoryController.class);

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
