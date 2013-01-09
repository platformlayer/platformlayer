package org.platformlayer.ops.filesystem;

import java.io.File;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.tree.OpsTreeBase;

public abstract class ManagedFilesystemItem extends OpsTreeBase {
	// static final Logger log = LoggerFactory.getLogger(ManagedFilesystemOpsItem.class);
	//
	public File filePath;
	public String fileMode = "0400";
	public String owner = null;
	public String group = null;

	protected File getFilePath() throws OpsException {
		return filePath;
	}

	// public final File getFilePath() {
	// return filePath;
	// }
	//
	// public final void setFilePath(File filePath) {
	// this.filePath = filePath;
	// }
	//
	// public final String getFileMode() {
	// return fileMode;
	// }

	public ManagedFilesystemItem setFileMode(String fileMode) {
		this.fileMode = fileMode;
		return this;
	}

	// public final String getOwner() {
	// return owner;
	// }

	public ManagedFilesystemItem setOwner(String owner) {
		this.owner = owner;
		return this;
	}

	// public final String getGroup() {
	// return group;
	// }

	public ManagedFilesystemItem setGroup(String group) {
		this.group = group;
		return this;
	}

	Command updateAction = null;
	Command deleteAction = null;

	// protected FileMetadata getFileMetadata() {
	// return new FileMetadata(getFileMode(), getOwner(), getGroup());
	// }

	protected void configureOwnerAndMode(OpsTarget target, FilesystemInfo fsInfo) throws OpsException {
		File file = getFilePath();

		if (owner != null || group != null) {
			boolean dirty = false;

			if (fsInfo == null || (owner != null && !fsInfo.matchesOwner(owner))) {
				dirty = true;
			}

			if (fsInfo == null || (group != null && !fsInfo.matchesOwner(group))) {
				dirty = true;
			}

			if (dirty) {
				boolean recursive = false;
				boolean dereferenceSymlinks = false;
				target.chown(file, owner, group, recursive, dereferenceSymlinks);
			}
		}

		if (fileMode != null) {
			if (fsInfo != null && fsInfo.isSymlink()) {
				throw new IllegalArgumentException("File mode is meaningless on symlink (filePath=" + filePath + ")");
			}

			if (fsInfo == null || !fsInfo.matchesMode(fileMode)) {
				target.chmod(file, fileMode);
			}
		}
	}

	protected void validateOwner(FilesystemInfo fsInfo) {
		if (owner != null) {
			if (!fsInfo.matchesOwner(owner)) {
				OpsContext.get().addWarning(this, "WrongOwner",
						"Owner was not as expected.  Expected=" + owner + " Actual=" + fsInfo.owner);
			}
		}

		if (group != null) {
			if (!fsInfo.matchesGroup(group)) {
				OpsContext.get().addWarning(this, "WrongGroup",
						"File group was not as expected.  Expected=" + group + " Actual=" + fsInfo.group);
			}
		}
	}

	protected void validateMode(FilesystemInfo fsInfo) {
		if (fileMode != null) {
			if (!fsInfo.matchesMode(fileMode)) {
				OpsContext.get().addWarning(
						this,
						"WrongMode",
						"Mode was not as expected.  Expected=" + fileMode + " Actual=" + fsInfo.getFileMode() + "("
								+ fsInfo.mode + ")");
			}
		}
	}

	protected void doUpdateAction(OpsTarget target) throws OpsException {
		// log.debug("Was updated; restarting dependencies");
		// markDependenciesForRestart(operation, null);

		if (getUpdateAction() != null) {
			target.executeCommand(getUpdateAction());
		}
	}

	protected void doDeleteAction(OpsTarget target) throws OpsException {
		if (getDeleteAction() != null) {
			target.executeCommand(getDeleteAction());
		}
	}

	public Command getUpdateAction() {
		return updateAction;
	}

	public Command getDeleteAction() {
		return deleteAction;
	}

	public ManagedFilesystemItem setUpdateAction(Command updateAction) {
		this.updateAction = updateAction;
		return this;
	}

	@Override
	protected void addChildren() throws OpsException {
	}

	// public String getGroup() {
	// return group;
	// }
	//
	// public void setGroup(String group) {
	// this.group = group;
	// }
	//
	// protected FileObject resolveFileObject(String vfsUrl) throws FileSystemException {
	// return getOpsSystem().resolveFileObject(vfsUrl);
	// }
	//
	// protected Md5Hash getMd5(String vfsPath) throws OpsException {
	// try {
	// FileObject fileObject = resolveFileObject(vfsPath);
	//
	// return getOpsSystem().getMd5(fileObject);
	// } catch (IOException e) {
	// throw new OpsException("Could not get MD5 of " + vfsPath, e);
	// }
	// }

}