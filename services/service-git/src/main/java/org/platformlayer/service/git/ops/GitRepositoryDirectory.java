package org.platformlayer.service.git.ops;

import java.io.File;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.backups.BackupDirectory;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.git.model.GitRepository;

public class GitRepositoryDirectory extends OpsTreeBase {
	@Handler
	public void handler(GitRepository model) {
	}

	@Override
	protected void addChildren() throws OpsException {
		GitRepository model = OpsContext.get().getInstance(GitRepository.class);

		File gitBase = new File("/var/git");
		File repoDir = new File(gitBase, model.name);

		{
			ManagedDirectory dir = ManagedDirectory.build(repoDir, "755");
			addChild(dir);
		}

		{
			GitRepoInit initRepo = injected(GitRepoInit.class);
			initRepo.repoDir = repoDir;
			addChild(initRepo);
		}

		{
			BackupDirectory backup = injected(BackupDirectory.class);
			backup.itemKey = model.getKey();

			backup.backupRoot = repoDir;

			addChild(backup);
		}
	}
}
