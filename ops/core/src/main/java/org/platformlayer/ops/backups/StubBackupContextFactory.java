package org.platformlayer.ops.backups;

import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.OpsException;

@Singleton
public class StubBackupContextFactory implements BackupContextFactory {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(StubBackupContextFactory.class);

	@Override
	public BackupContext build(ItemBase item) throws OpsException {
		throw new UnsupportedOperationException();
	}
}
