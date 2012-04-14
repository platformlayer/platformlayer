package org.platformlayer.service.zookeeper.ops;

import java.io.File;

import javax.inject.Inject;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.UniqueTag;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.filesystem.ManagedSymlink;
import org.platformlayer.ops.filesystem.SimpleFile;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.supervisor.SupervisorInstance;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.network.v1.NetworkConnection;
import org.platformlayer.service.zookeeper.model.ZookeeperServer;

import com.google.common.base.Objects;

public class ZookeeperInstance extends OpsTreeBase {
	@Inject
	PlatformLayerHelpers platformLayer;

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		String supervisorKey = "zookeeper";
		ZookeeperInstanceModel template = injected(ZookeeperInstanceModel.class);

		ZookeeperServer model = template.getModel();

		File instanceDir = template.getInstanceDir();

		addChild(ManagedDirectory.build(instanceDir, "0555"));
		addChild(ManagedDirectory.build(new File(instanceDir, "data"), "0755"));
		addChild(ManagedDirectory.build(new File(instanceDir, "logs"), "0755"));

		addChild(TemplatedFile.build(template, new File(instanceDir, "data/myid")).setFileMode("0755"));

		addChild(SimpleFile.build(getClass(), new File(instanceDir, "log4j.properties")).setFileMode("0444"));
		// addChild(SimpleFile.build(getClass(),
		// new File(instanceDir, "start-zookeeper.sh"))
		// .setFileMode("0555"));

		addChild(TemplatedFile.build(template, new File(instanceDir, "zookeeper.cfg")).setFileMode("0444"));

		// TODO: Firewall?
		// addChildren(FirewallEntry.openPortForOpsSystem(this, PORT));
		//
		// TODO: Supervisor
		// OpsItem service = addChild(buildService());
		// service.dependsOn(confLog4j);
		// service.dependsOn(confZookeeper);
		// service.dependsOn(startFile);
		//
		// TODO: Monitor logs
		// addChild(buildWatchLogFile(getInstanceDir().join("logs/zookeeper.log")));

		// TODO: Add backup
		// {
		// BackupDirectory backup = injected(BackupDirectory.class);
		// backup.itemKey = model.getKey();
		//
		// File jenkinsRoot = new File("/var/lib/jenkins");
		// backup.backupRoot = jenkinsRoot;
		//
		// String[] excludes = { "jobs/*/workspace", "jobs/*/modules",
		// "jobs/*/builds/*/workspace.tar.gz", ".m2/repository" };
		//
		// for (String exclude : excludes) {
		// backup.excludes.add(new File(jenkinsRoot, exclude));
		// }
		//
		// instance.addChild(backup);
		// }

		// Note: Don't use supervisord.conf, otherwise supervisorctl will fail with:
		// "Error: .ini file does not include supervisorctl section"
		addChild(TemplatedFile.build(template, new File(instanceDir, "zookeeper.conf")).setFileMode("0444"));

		{
			ManagedSymlink symlink = ManagedSymlink.build(
					new File("/etc/supervisor/conf.d/" + supervisorKey + ".conf"), new File(instanceDir,
							"supervisor.conf"));
			addChild(symlink);
		}

		{
			for (ZookeeperServer peer : template.getClusterServers()) {
				if (Objects.equal(peer.getKey(), model.getKey())) {
					continue;
				}

				for (int systemPort : ZookeeperConstants.SYSTEM_PORTS) {
					NetworkConnection networkConnection = injected(NetworkConnection.class);
					networkConnection.setDestItem(peer.getKey());
					networkConnection.setSourceItem(model.getKey());
					networkConnection.setPort(systemPort);
					networkConnection.setProtocol("tcp");

					networkConnection.getTags().add(Tag.buildParentTag(model.getKey()));
					Tag uniqueTag = UniqueTag.build(model, peer, String.valueOf(systemPort));
					networkConnection.getTags().add(uniqueTag);

					String id = model.clusterId + "-" + peer.clusterId + "-" + systemPort;
					networkConnection.key = PlatformLayerKey.fromId(id);

					// Hmmm... when do we embed, and when do we not??
					// We need some clear rules!
					// Here, we don't embed because it's cross-machine
					platformLayer.putItemByTag(networkConnection, uniqueTag);
				}
			}
		}

		{
			SupervisorInstance service = injected(SupervisorInstance.class);
			service.id = supervisorKey;
			addChild(service);
		}
	}
}
