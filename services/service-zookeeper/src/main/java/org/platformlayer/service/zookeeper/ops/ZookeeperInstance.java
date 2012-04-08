package org.platformlayer.service.zookeeper.ops;

import java.io.File;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.filesystem.ManagedSymlink;
import org.platformlayer.ops.filesystem.SimpleFile;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.metrics.collectd.OpsTreeBase;
import org.platformlayer.ops.supervisor.SupervisorInstance;

public class ZookeeperInstance extends OpsTreeBase {
	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		String supervisorKey = "zookeeper";
		ZookeeperInstanceModel model = injected(ZookeeperInstanceModel.class);

		File instanceDir = model.getInstanceDir();
		
		addChild(ManagedDirectory.build(instanceDir, "0555"));
		addChild(ManagedDirectory.build(new File(instanceDir, "data"), "0755"));
		addChild(ManagedDirectory.build(new File(instanceDir, "logs"), "0755"));

		addChild(SimpleFile.build(getClass(),
				new File(instanceDir, "log4j.properties")).setFileMode("0444"));
//		addChild(SimpleFile.build(getClass(),
//				new File(instanceDir, "start-zookeeper.sh"))
//				.setFileMode("0555"));

		addChild(TemplatedFile.build(model,
				new File(instanceDir, "zookeeper.cfg")).setFileMode("0444"));

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

		addChild(TemplatedFile.build(model,
				new File(instanceDir, "supervisord.conf")).setFileMode("0444"));

		{
			ManagedSymlink symlink = ManagedSymlink.build(new File(
					"/etc/supervisor/conf.d/" + supervisorKey + ".conf"),
					new File(instanceDir, "supervisord.conf"));
			addChild(symlink);
		}

		{
			SupervisorInstance service = injected(SupervisorInstance.class);
			service.id = supervisorKey;
			addChild(service);
		}
	}
}
