package org.platformlayer.ops.packages;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.*;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Deviations;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

public class UpdatePackages {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(UpdatePackages.class);

	@Inject
	AptPackageManager apt;

	@Handler
	public void handler(OpsTarget target) throws OpsException {
		apt.update(target, true);

		List<String> outOfDatePackage = apt.findOutOfDatePackages(target);

		if (!outOfDatePackage.isEmpty()) {
			// Pre-download any out-of-date files; will make any maintenance window smaller
			Command command = Command.build("apt-get --yes --download-only dist-upgrade");
			target.executeCommand(command);
		}

		Deviations.assertEquals(Collections.emptyList(), outOfDatePackage, "Packaged out of date");
	}
}
