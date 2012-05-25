package org.platformlayer.ops.backups;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.machines.PlatformLayerCloudHelpers;
import org.platformlayer.ops.machines.StorageConfiguration;

public class BackupContextFactory {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(BackupContextFactory.class);

	@Inject
	Provider<BackupContext> backupContextProvider;

	@Inject
	InstanceHelpers instances;

	@Inject
	PlatformLayerCloudHelpers cloud;

	public BackupContext build(ItemBase item) throws OpsException {
		Machine machine = instances.findMachine(item);
		if (machine == null) {
			throw new OpsException("Cannot determine machine for: " + item);
		}
		StorageConfiguration storageConfiguration = cloud.getStorageConfiguration(machine);
		return build(storageConfiguration);
	}

	public BackupContext build(StorageConfiguration storageConfiguration) throws OpsException {
		// TODO: Should be configurable
		// TODO: Configure cloud target here?
		String containerName = "backups";
		String backupId = UUID.randomUUID().toString();

		BackupContext context = backupContextProvider.get();
		context.data.id = backupId;
		context.containerName = containerName;

		context.credentials = storageConfiguration.getOpenstackCredentials();

		return context;
	}
}
