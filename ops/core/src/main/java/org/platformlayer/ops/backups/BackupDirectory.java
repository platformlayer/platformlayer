package org.platformlayer.ops.backups;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.BackupAction;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.tree.OpsTreeBase;

import com.google.common.collect.Lists;

public class BackupDirectory extends OpsTreeBase {
	static final Logger log = Logger.getLogger(BackupDirectory.class);
	private static final String FORMAT = "tgz_directory";

	public File backupRoot;
	public List<File> excludes = Lists.newArrayList();
	public PlatformLayerKey itemKey;

	@Handler
	public void doOperation() throws OpsException, IOException {
	}

	@Inject
	BackupHelpers backups;

	@Handler(BackupAction.class)
	public void doBackup() throws OpsException, IOException {
		OpsContext opsContext = OpsContext.get();

		// Machine machine = opsContext.getInstance(Machine.class);
		OpsTarget target = opsContext.getInstance(OpsTarget.class);

		BackupContext backup = backups.getContext();

		DirectoryBackup request = new DirectoryBackup();
		request.target = target;
		request.rootDirectory = backupRoot;
		request.exclude.addAll(this.excludes);

		backup.doBackup(request);

		backup.add(new BackupItem(itemKey, FORMAT, request.objectName));
	}

	@Override
	protected void addChildren() throws OpsException {
	}
}
