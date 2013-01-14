package org.platformlayer.service.jenkins.ops;

import java.io.File;
import java.util.List;

import org.platformlayer.ops.FileUpload;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.ssh.SshAuthorizedKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class EnsureKnownHost {
	private static final Logger log = LoggerFactory.getLogger(EnsureKnownHost.class);

	public String user = "root";

	public File homeDir;

	public String host;
	public String algorithm;
	public String key;

	@Handler
	public void handler(OpsTarget target) throws OpsException {
		if (Strings.isNullOrEmpty(host) || Strings.isNullOrEmpty(algorithm) || Strings.isNullOrEmpty(key)) {
			throw new IllegalStateException();
		}

		if (homeDir == null) {
			homeDir = SshAuthorizedKey.getDefaultHomedir(user);
		}

		File knownHostsPath = new File(homeDir, ".ssh/known_hosts");

		String knownHosts = target.readTextFile(knownHostsPath);
		if (knownHosts == null) {
			knownHosts = "";
		}

		boolean found = false;

		for (String line : Splitter.on('\n').split(knownHosts)) {
			line = line.trim();
			if (line.startsWith(host)) {
				List<String> tokens = Lists.newArrayList(Splitter.on(' ').omitEmptyStrings().split(line));
				if (tokens.size() != 3) {
					continue;
				}

				if (!tokens.get(0).equals(host)) {
					continue;
				}

				if (!tokens.get(1).equals(algorithm)) {
					continue;
				}
				if (!tokens.get(2).equals(key)) {
					continue;
				}

				found = true;
			}
		}

		if (!found) {
			if (OpsContext.isValidate()) {
				log.error("SSH host key not found in " + knownHostsPath);
			}

			if (OpsContext.isConfigure()) {
				// TODO: Append atomically?
				log.info("Adding known_host key to " + knownHostsPath);
				String desiredLine = host + " " + algorithm + " " + key;
				if (!knownHosts.endsWith("\n")) {
					if (!Strings.isNullOrEmpty(knownHosts)) {
						knownHosts += "\n";
					}
				}
				knownHosts += desiredLine + "\n";
				FileUpload.upload(target, knownHostsPath, knownHosts);
				target.chown(knownHostsPath, user, null, false, false);
			}
		}
	}
}
