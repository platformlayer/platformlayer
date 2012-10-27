package org.platformlayer.ops.backups;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsException;

public interface BackupContext {

	@Deprecated
	// TODO: Merge into backup action
	void add(BackupItem backupItem);

	void doBackup(DirectoryBackup request) throws OpsException;

	void writeDescriptor() throws OpsException;

	void uploadStream(Backup request, Command dumpAll) throws OpsException;

}
