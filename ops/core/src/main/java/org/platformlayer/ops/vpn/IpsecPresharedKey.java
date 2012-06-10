package org.platformlayer.ops.vpn;

import java.io.File;

import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.FileUpload;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

import com.google.common.base.Splitter;

public class IpsecPresharedKey {
	public static final String SHAREDKEY_USER_FQDN = "sharedkey@platformlayer.org";

	public String id;
	public Secret secret;

	@Handler
	public void handler(OpsTarget target) throws OpsException {
		if (OpsContext.isConfigure()) {
			File pskFile = new File("/etc/racoon/psk.txt");

			String psk = target.readTextFile(pskFile);
			if (psk == null) {
				psk = "# Managed by PlatformLayer\n";
			}

			boolean found = false;

			for (String line : Splitter.on("\n").split(psk)) {
				line = line.trim();
				if (line.startsWith("#")) {
					continue;
				}

				if (!line.startsWith(id)) {
					continue;
				}

				String tail = line.substring(id.length());
				if (tail.isEmpty()) {
					continue;
				}

				char firstChar = tail.charAt(0);
				if (!Character.isWhitespace(firstChar)) {
					continue;
				}

				tail = tail.trim();
				if (tail.equals(secret.plaintext())) {
					found = true;
				}
			}

			if (!found) {
				psk = psk + "\n";
				psk += id + " " + secret.plaintext() + "\n";

				FileUpload.upload(target, pskFile, psk);

				target.executeCommand(Command.build("racoonctl reload-config"));
			}
		}
	}
}
