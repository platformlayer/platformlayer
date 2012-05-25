package org.platformlayer.ops.packages;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Deviations;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.AptHelper;

public class UpdatePackages {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(UpdatePackages.class);

	@Inject
	AptHelper apt;

	@Handler
	public void handler(OpsTarget target) throws OpsException {
		apt.update(target);

		List<String> outOfDatePackage = AptPackageManager.findOutOfDatePackages(target);

		if (!outOfDatePackage.isEmpty()) {
			// Pre-download any out-of-date files; will make any maintenance window smaller
			Command command = Command.build("apt-get --yes --download-only dist-upgrade");
			target.executeCommand(command);
		}

		Deviations.assertEquals(Collections.emptyList(), outOfDatePackage, "Packaged out of date");
	}
}
