package org.platformlayer.ops.firewall.scripts;

import java.io.File;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.filesystem.SimpleFile;
import org.platformlayer.ops.tree.OpsTreeBase;

public class PersistIptablesScripts extends OpsTreeBase {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(PersistIptablesScripts.class);

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		File iptablesDir = new File("/etc/iptables");
		addChild(ManagedDirectory.build(iptablesDir, "0644"));

		addChild(SimpleFile.build(getClass(), new File("/etc/network/if-pre-up.d/iptables-lockdown"))
				.setFileMode("755").setUpdateAction(Command.build("/etc/network/if-pre-up.d/iptables-lockdown")));
		addChild(SimpleFile.build(getClass(), new File("/etc/network/if-up.d/iptables-ifup")).setFileMode("755"));
	}
}
