package org.platformlayer.ops.backups;

import javax.inject.Inject;
import javax.inject.Provider;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;

public class BackupHelpers {
	public BackupContext getContext() {
		BackupContext context = OpsContext.get().getInstance(BackupContext.class);
		if (context == null) {
			throw new IllegalStateException();
		}
		return context;
	}

	@Inject
	Provider<BackupContextFactory> backupContextFactory;

	public BackupContext createBackupContext(ItemBase item) throws OpsException {
		// BackupContextFactory backupContextFactory = opsSystem.getBackupContextFactory();
		BackupContext backupContext = backupContextFactory.get().build(item);
		return backupContext;
	}

	// public BackupContextFactory getBackupContextFactory() {
	// return backupContextFactory.get();
	// }

}
