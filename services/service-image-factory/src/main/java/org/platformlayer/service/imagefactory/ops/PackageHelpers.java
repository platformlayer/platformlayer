package org.platformlayer.service.imagefactory.ops;

import javax.inject.Inject;

import org.platformlayer.ops.OpsContext;
import org.platformlayer.service.imagefactory.OperatingSystem;

public class PackageHelpers {
	@Inject
	OpsContext opsContext;

	// public String findPackageFor(String command, OperatingSystem operatingSystem) {
	// List<String> keys = Lists.newArrayList(command, operatingSystem.getDistribution().toString(),
	// operatingSystem.getVersion());
	// return doKeyLookup(keys);
	// }

	// public String getPackageFor(String command, OperatingSystem operatingSystem) {
	// String packageName = findPackageFor(command, operatingSystem);
	// if (packageName == null) {
	// throw new IllegalArgumentException("Cannot determine package for " + command + " on " + operatingSystem);
	// }
	// return packageName;
	// }

	// private String doKeyLookup(Iterable<String> keys) {
	// List<String> lowerKeys = Lists.newArrayList();
	// for (String key : keys) {
	// lowerKeys.add(key.toLowerCase());
	// }
	//
	// for (int i = lowerKeys.size(); i >= 1; i--) {
	// String key = Joiner.on('.').join(lowerKeys.subList(0, i));
	// String value = opsContext.getConfiguration().lookup(key, null);
	// if (value != null) {
	// return value;
	// }
	// }
	//
	// return null;
	// }

	public String getDefaultKernelPackage(OperatingSystem operatingSystem) {
		switch (operatingSystem.getDistribution()) {
		case Debian:
			return "linux-image-2.6-amd64";
		case Ubuntu:
			return "linux-image-server";

		default:
			throw new IllegalStateException();
		}
	}

	// if (isUbuntu) {
	// target.executeCommand(Command.build("apt-get install --yes qemu-kvm").setTimeout(TimeSpan.FIVE_MINUTES));
	// } else {
	// target.executeCommand(Command.build("apt-get install --yes qemu-utils").setTimeout(TimeSpan.FIVE_MINUTES));
	// }
}
