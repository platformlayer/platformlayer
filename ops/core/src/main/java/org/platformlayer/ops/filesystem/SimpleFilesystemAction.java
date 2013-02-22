package org.platformlayer.ops.filesystem;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

public class SimpleFilesystemAction implements FilesystemAction {
	final Command command;

	public SimpleFilesystemAction(Command command) {
		super();
		this.command = command;
	}

	@Override
	public void execute(OpsTarget target, ManagedFilesystemItem managedFilesystemItem) throws OpsException {
		target.executeCommand(command);
	}

}
