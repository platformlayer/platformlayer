package org.platformlayer.ops.vpn;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.slf4j.*;
import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.FileUpload;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class IpsecPresharedKey {
	static final Logger log = LoggerFactory.getLogger(IpsecPresharedKey.class);

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

			// TODO: Extend MapSplitter / add some helper functions??
			Splitter keyValueSpliter = Splitter.on(CharMatcher.WHITESPACE).limit(2).omitEmptyStrings().trimResults();

			Map<String, String> psks = Maps.newHashMap();

			for (String line : Splitter.on("\n").trimResults().omitEmptyStrings().split(psk)) {
				if (line.startsWith("#")) {
					continue;
				}

				List<String> tokens = Lists.newArrayList(keyValueSpliter.split(line));
				if (tokens.size() != 2) {
					throw new OpsException("Cannot parse PSK line: " + line);
				}

				String key = tokens.get(0);
				String value = tokens.get(1);

				if (psks.containsKey(key)) {
					// (We could check to see if they're the same, but this is generally not good)
					throw new OpsException("Found duplicate PSK");
				}
				psks.put(key, value);

				if (!key.equals(id)) {
					continue;
				}

				if (value.equals(secret.plaintext())) {
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
