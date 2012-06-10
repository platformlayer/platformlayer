package org.platformlayer.ops.vpn;

import java.io.File;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

/**
 * Removes the psk.txt file shipped with debian. Not sure why it ships at all!
 * 
 */
public class IpsecBootstrap {
	static final Logger log = Logger.getLogger(IpsecBootstrap.class);

	@Handler
	public void handler(OpsTarget target) throws OpsException {
		if (OpsContext.isConfigure()) {
			File pskFile = new File("/etc/racoon/psk.txt");

			String psk = target.readTextFile(pskFile);
			if (psk != null && psk.contains("mekmitasdigoat")) {
				log.warn("Removing default Debian psk.txt file");
				target.mv(pskFile, new File("/etc/racoon/psk.txt.example"));
			}
		}
	}

}
