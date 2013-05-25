package org.platformlayer.service.cloud.direct.ops;

import java.io.File;
import java.io.IOException;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.FileUpload;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.filesystem.FilesystemInfo;
import org.platformlayer.ops.tree.OpsItemBase;

public class MountCgroups extends OpsItemBase {
	@Handler
	public void handler() throws OpsException, IOException {
		// TODO: Only if not installed
		OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);

		File cgroupsFile = new File("/cgroup");
		FilesystemInfo info = target.getFilesystemInfoFile(cgroupsFile);
		if (info != null) {
			// TODO: Better idempotency
			return;
		}

		String fstabLine = "\ncgroup\t/cgroup\tcgroup\tdefaults\t0\t0";
		File fstabFile = new File("/etc/fstab");
		String fstab = target.readTextFile(fstabFile);
		fstab += fstabLine;
		FileUpload.upload(target, fstabFile, fstab);

		target.mkdir(cgroupsFile);

		Command mountCommand = Command.build("mount cgroup");
		target.executeCommand(mountCommand);

		// mkdir /cgroup
		// echo -e "cgroup\t/cgroup\tcgroup\tdefaults\t0\t0" >> /etc/fstab
		// mount cgroup
	}

}
