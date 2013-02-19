package org.platformlayer.ops.bootstrap;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

		command = Command
				.build("apt-get remove --yes groff-base consolekit sane-utils  exim4-config installation-report");
		target.executeCommand(command);

		// Replace netcat with netcat6
		target.executeCommand("apt-get install netcat6");
		target.executeCommand("apt-get remove netcat-traditional");

		// We just want headless...
		command = Command
				.build("apt-get remove --yes dbus openjdk-7-jdk openjdk-7-jre  icedtea-7-jre-cacao icedtea-7-jre-jamvm");
		target.executeCommand(command);

		// Do we need python?? Does it still get installed??
		// apt-get remove python
		// Warnings??
		// apt-get remove install-info
		//
		// Squeeze:
		// apt-get remove aptitude tasksel tasksel-data man-db manpages libxapian22 info

		target.executeCommand("apt-get autoremove");

	}
}
