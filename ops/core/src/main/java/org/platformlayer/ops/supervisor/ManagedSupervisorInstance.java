package org.platformlayer.ops.supervisor;

import java.io.File;

import org.openstack.utils.Utf8;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.SyntheticFile;
import org.platformlayer.ops.tree.OpsTreeBase;

public class ManagedSupervisorInstance extends OpsTreeBase {
	public SupervisorProcessConfig config;

	File getConfigFile() {
		return new File("/etc/supervisor/conf.d/", getId() + ".conf");
	}

	private String getId() {
		return config.getId();
	}

	SupervisorProcessConfig getSupervisorProcess() {
		return config;
	}

	public static class SupervisorConfigFile extends SyntheticFile {
		public ManagedSupervisorInstance parent;

		@Override
		protected byte[] getContentsBytes() throws OpsException {
			return Utf8.getBytes(parent.getSupervisorProcess().buildConfigFile());
		}

		public SupervisorConfigFile setParent(ManagedSupervisorInstance parent) {
			this.parent = parent;
			return this;
		}
	}

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		{
			SupervisorConfigFile conf = injected(SupervisorConfigFile.class);
			conf.setParent(this);
			conf.filePath = getConfigFile();
			addChild(conf);
		}

		{
			SupervisorInstance run = injected(SupervisorInstance.class);
			run.id = getId();
			addChild(run);
		}
	}
}
