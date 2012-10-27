package org.platformlayer.ops.backups;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.OpsException;

public interface BackupContextFactory {

	BackupContext build(ItemBase item) throws OpsException;

}
