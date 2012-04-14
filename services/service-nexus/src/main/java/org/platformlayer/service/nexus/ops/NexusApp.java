package org.platformlayer.service.nexus.ops;

import java.io.File;
import java.io.IOException;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

public class NexusApp {
	@Handler
	public void handler() throws IOException, OpsException {
		// TODO: This needs to be idempotent
		OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);

		String url = "http://nexus.sonatype.org/downloads/all/nexus-webapp-1.9.2.4.war";
		File warFile = new File("/var/lib/jetty/wars/nexus-webapp-1.9.2.4.war");

		target.executeCommand("wget {0} -O {1}", url, warFile);

		// Whatever version of nexus we have, we want it to be the root
		target.symlink(warFile, new File("/var/lib/jetty/webapps/root.war"), false);
	}

	public static NexusApp build() {
		return Injection.getInstance(NexusApp.class);
	}
}
