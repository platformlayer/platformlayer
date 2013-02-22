package org.platformlayer.ops.firewall.scripts;

import java.io.File;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.CommandEnvironment;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.filesystem.FilesystemAction;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.filesystem.ManagedFilesystemItem;
import org.platformlayer.ops.filesystem.SimpleFile;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistIptablesScripts extends OpsTreeBase {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(PersistIptablesScripts.class);

	public static final File BASE_DIR = new File("/etc/iptables");

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		addChild(ManagedDirectory.build(BASE_DIR, "0644"));

		addChild(SimpleFile.build(getClass(), new File("/etc/network/if-pre-up.d/iptables-lockdown"))
				.setFileMode("755").setUpdateAction(new FilesystemAction() {

					@Override
					public void execute(OpsTarget target, ManagedFilesystemItem managedFilesystemItem)
							throws OpsException {
						if (managedFilesystemItem.getNewFileWasCreated()) {
							// Set the parameters the ifup sets
							CommandEnvironment env = new CommandEnvironment();
							env.put("MODE", "start");
							env.put("IFACE", "--all");
							env.put("ADDRFAM", "meta");

							Command runLockdown = Command.build("/etc/network/if-pre-up.d/iptables-lockdown");
							runLockdown.setEnvironment(env);
							target.executeCommand(runLockdown);
						}
					}
				}));

		addChild(SimpleFile.build(getClass(), new File("/etc/network/if-up.d/iptables-ifup")).setFileMode("755"));
	}
}
