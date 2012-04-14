package org.platformlayer.service.dns.ops;

import java.io.File;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

public class TinyDnsBootstrap {

	@Handler
	public void handler() throws OpsException {
		OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);

		if (target.getFilesystemInfoFile(new File("/var/tinydns")) == null) {
			target.executeCommand("tinydns-conf tinydns dnslog /var/tinydns 0.0.0.0");
		}
	}

	public static TinyDnsBootstrap build() {
		return Injection.getInstance(TinyDnsBootstrap.class);
	}

}
