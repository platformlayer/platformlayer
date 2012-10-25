package org.platformlayer.client.cli.commands;

import org.kohsuke.args4j.Argument;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.core.model.BackupAction;

public class BackupItem extends ActionCommandBase {
	@Argument(index = 0, usage = "path", required = true)
	public ItemPath path;

	public BackupItem() {
		super("backup");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException {
		BackupAction action = new BackupAction();
		return runAction(path, action);
	}
}
