package org.platformlayer.ops.bootstrap;

import java.io.File;

import org.slf4j.*;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.SimpleFile;
import org.platformlayer.ops.tree.OpsTreeBase;

public class ConfigureSshd extends OpsTreeBase {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ConfigureSshd.class);

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		addChild(SimpleFile.build(getClass(), new File("/etc/ssh/sshd_config")).setFileMode("0644").setOwner("root")
				.setGroup("root").setUpdateAction(Command.build("sudo service ssh reload || service ssh reload")));
	}
}
