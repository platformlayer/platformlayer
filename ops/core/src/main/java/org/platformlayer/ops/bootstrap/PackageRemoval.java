package org.platformlayer.ops.bootstrap;

import org.slf4j.*;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

public class PackageRemoval {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(PackageRemoval.class);

	@Handler
	public void handler(OpsTarget target) throws OpsException {
		// Make sure openssh-server is manually installed (i.e. not through task-ssh-server)
		target.executeCommand("apt-get install openssh-server");

		// Only for wheezy? Squeeze has a depencency of install-info -> findutils
		Command command = Command
				.build("apt-get remove --yes aptitude tasksel tasksel-data man-db manpages libxapian22 libboost-iostreams1.49.0 info ");
		target.executeCommand(command);

		// Maybe:
		// apt-get remove groff-base
		// apt-get install netcat6
		// apt-get remove netcat-traditional

		// apt-get remove consolekit
		// apt-get remove sane-utils

		// We just want headless...
		// apt-get remove openjdk-7-jdk openjdk-7-jre
		// apt-get remove icedtea-7-jre-cacao
		// apt-get remove icedtea-7-jre-jamvm

		// Do we need python?? Does it still get installed??
		// apt-get remove python
		// Warnings??
		// apt-get remove install-info

		// Squeeze:
		// apt-get remove aptitude tasksel tasksel-data man-db manpages libxapian22 info

	}
}
