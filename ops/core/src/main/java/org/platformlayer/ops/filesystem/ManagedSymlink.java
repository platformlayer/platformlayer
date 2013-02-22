package org.platformlayer.ops.filesystem;

import java.io.File;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.HasDescription;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

//@Icon("folder")
public class ManagedSymlink extends ManagedFilesystemItem implements HasDescription {
	static final Logger log = LoggerFactory.getLogger(ManagedSymlink.class);

	public File symlinkTarget;

	private boolean createdNewFile;

	public ManagedSymlink() {
		// Default mode is meaningless on a symlink
		setFileMode(null);
	}

	public static ManagedSymlink build(File alias, File target) {
		ManagedSymlink link = OpsContext.get().getInjector().getInstance(ManagedSymlink.class);
		link.filePath = alias;
		link.symlinkTarget = target;
		return link;
	}

	@Handler
	public void doConfigureValidate(OpsTarget target) throws Exception {
		File filePath = getFilePath();
		FilesystemInfo fsInfo = target.getFilesystemInfoFile(filePath);
		boolean exists = (fsInfo != null);

		File symlinkTarget = getSymlinkTarget();

		boolean symlinkTargetMatch = false;
		if (fsInfo != null) {
			symlinkTargetMatch = Objects.equal(fsInfo.symlinkTarget, symlinkTarget.toString());
		}

		if (OpsContext.isConfigure()) {
			if (!exists) {
				target.symlink(symlinkTarget, filePath, false);
				createdNewFile = true;

				doUpdateAction(target);
			} else {
				if (!symlinkTargetMatch) {
					target.symlink(symlinkTarget, filePath, true);
				}
			}

			configureOwnerAndMode(target, fsInfo);

			// FilesystemInfo targetInfo = agent.getFilesystemInfoFile(getSymlinkTarget());
			// configureOwnerAndMode(agent, targetInfo, getSymlinkTarget());
		}

		if (OpsContext.isValidate()) {
			if (!exists) {
				OpsContext.get().addWarning(this, "DoesNotExist", "Symlink not found: " + filePath);
			} else if (!symlinkTargetMatch) {
				OpsContext.get().addWarning(this, "TargetMismatch", "Symlink points at wrong target: " + fsInfo);

				validateOwner(fsInfo);

				// Mode is meaningless on symlinks
				// validateMode(fsInfo);
			}
		}

		if (OpsContext.isDelete()) {
			if (exists) {
				target.rm(filePath);
				doUpdateAction(target);
			}
		}
	}

	protected File getSymlinkTarget() {
		return symlinkTarget;
	}

	@Override
	public String getDescription() throws Exception {
		return "Symlink: " + getFilePath() + " -> " + getSymlinkTarget();
	}

	@Override
	public boolean getNewFileWasCreated() {
		return createdNewFile;
	}

}
