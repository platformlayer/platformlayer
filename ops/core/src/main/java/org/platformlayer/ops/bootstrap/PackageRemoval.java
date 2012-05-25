package org.platformlayer.ops.bootstrap;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

public class PackageRemoval {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(PackageRemoval.class);

	@Handler
	public void handler(OpsTarget target) throws OpsException {
		// Only for wheezy? Squeeze has a depencency of install-info -> findutils
		Command command = Command
				.build("apt-get remove --yes aptitude tasksel tasksel-data man-db manpages libxapian22 libboost-iostreams1.49.0 info install-info");
		target.executeCommand(command);

		// Squeeze:
		// apt-get remove aptitude tasksel tasksel-data man-db manpages libxapian22 info

	}
}
