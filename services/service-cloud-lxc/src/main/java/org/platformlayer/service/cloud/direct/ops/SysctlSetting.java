package org.platformlayer.service.cloud.direct.ops;

import java.io.File;

import org.apache.log4j.Logger;
import org.openstack.utils.Utf8;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.filesystem.SyntheticFile;

public class SysctlSetting extends SyntheticFile {
	private static final Logger log = Logger.getLogger(SysctlSetting.class);

	public String key;
	public String value;

	@Override
	protected File getFilePath() throws OpsException {
		File base = new File("/etc/sysctl.d");
		// Files must end in .conf
		return new File(base, key + ".conf");
	}

	@Override
	protected void doUpdateAction(OpsTarget target) throws OpsException {
		super.doUpdateAction(target);

		target.executeCommand(Command.build("sysctl --load={0}", getFilePath()));
	}

	@Override
	protected void doDeleteAction(OpsTarget target) throws OpsException {
		super.doDeleteAction(target);
		log.warn("Delete of SysCtl setting does not reset value");
	}

	@Override
	protected byte[] getContentsBytes() throws OpsException {
		StringBuilder sb = new StringBuilder();
		sb.append("# Managed by PlatformLayer\n");
		sb.append(key + "=" + value + "\n");

		return Utf8.getBytes(sb.toString());
	}

	public static SysctlSetting build(String key, String value) {
		SysctlSetting setting = injected(SysctlSetting.class);
		setting.key = key;
		setting.value = value;
		return setting;
	}

}
