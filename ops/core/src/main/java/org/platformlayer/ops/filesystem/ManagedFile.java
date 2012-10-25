package org.platformlayer.ops.filesystem;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.openstack.crypto.Md5Hash;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

import com.google.common.base.Objects;

//@Icon("document")
public abstract class ManagedFile extends ManagedFilesystemItem {
	static final Logger log = Logger.getLogger(ManagedFile.class);

	@Inject
	OpsContext ops;

	// private static final int EXTRACT_LENGTH = 64;
	//
	// public static final String ALERT_KEY_CONTENTSMISMATCH = "ContentsMismatch";
	//
	// String sourceFile;
	boolean onlyIfNotExists;

	public FileAccess mkdirs;

	//
	// boolean symlinkVersions = false;
	//
	// // String oldConfig = null;

	protected abstract void uploadFile(OpsTarget target, File remoteFilePath) throws IOException, OpsException;

	protected abstract Md5Hash getSourceMd5(OpsTarget target) throws OpsException;

	@Handler
	public void handler(OpsTarget target) throws Exception {
		// OpsServer server = smartGetServer(true);
		// Agent agent = server.getAgent();

		File filePath = getFilePath();

		Md5Hash sourceMd5 = null;
		if (!OpsContext.isDelete()) {
			sourceMd5 = getSourceMd5(target);
		}

		Md5Hash remoteMd5;
		if (target.isMachineTerminated()) {
			remoteMd5 = null;
		} else {
			remoteMd5 = target.getFileHash(filePath);
		}

		boolean isUpToDate = Objects.equal(sourceMd5, remoteMd5);

		if (OpsContext.isDelete()) {
			if (remoteMd5 != null) {
				target.rm(filePath);
				doDeleteAction(target);
			}
		}

		if (OpsContext.isConfigure()) {
			boolean changed = false;

			if (mkdirs != null) {
				if (remoteMd5 == null) {
					File dir = filePath.getParentFile();
					if (target.getFilesystemInfoFile(dir) == null) {
						// TODO: Can mkdir return true/false to indicate presence?
						target.mkdir(dir, mkdirs.mode);

						if (mkdirs.owner != null) {
							target.chown(dir, mkdirs.owner, mkdirs.group, false, false);
						}
					}
				}
			}

			boolean doUpload = false;
			if (!isUpToDate) {
				doUpload = true;
				if (isOnlyIfNotExists()) {
					if (remoteMd5 != null) {
						log.info("File already exists, and file set to create only if not exists: exiting");
						doUpload = false;
					}
				}
			}

			if (OpsContext.isForce()) {
				doUpload = true;
			}

			if (doUpload) {
				File newSymlinkDestination = null;
				if (isSymlinkVersions()) {
					throw new UnsupportedOperationException();

					// newSymlinkDestination = new FilePath(getRemoteFilePath() + "-" +
					// OpsSystem.buildSimpleTimeString());
					// uploadFile(newSymlinkDestination.asString());
					//
					// remoteMd5 = agent.getRemoteMd5(newSymlinkDestination);
				} else {
					uploadFile(target, filePath);
					remoteMd5 = target.getFileHash(filePath);
				}

				if (!Objects.equal(remoteMd5, sourceMd5)) {
					Md5Hash debugSourceMd5 = getSourceMd5(target);
					debugSourceMd5 = getSourceMd5(target);
					log.debug("debugSourceMd5: " + debugSourceMd5 + " vs " + remoteMd5);
					throw new IllegalStateException("Uploaded file, but contents did not then match: " + filePath);
				}

				if (newSymlinkDestination != null) {
					target.symlink(newSymlinkDestination, filePath, false);
				}

				changed = true;
			}

			{
				FilesystemInfo fsInfo = target.getFilesystemInfoFile(filePath);

				if (fsInfo.isSymlink()) {
					// TODO: Follow symlink for permissions
					throw new UnsupportedOperationException();
					// target = new FilePath(fsInfo.target);
					// fsInfo = agent.getFilesystemInfoFile(target);
				}

				// TODO: Should this trigger a change??
				configureOwnerAndMode(target, fsInfo);
			}

			if (changed) {
				doUpdateAction(target);

				// log.debug("Was updated; restarting dependencies");
				// markDependenciesForRestart(operation, null);
			}
		}

		if (OpsContext.isValidate()) {
			if (remoteMd5 == null) {
				ops.addWarning(this, "File does not exist: " + filePath);
				return;
			}

			if (!isUpToDate) {
				boolean raiseAlert = true;
				if (isOnlyIfNotExists()) {
					if (remoteMd5 != null) {
						log.info("File does not match, but file set to create only if not exists: ignoring");
						raiseAlert = false;
					}
				}

				if (raiseAlert) {
					if (sourceMd5 != null) {
						// int differncePosition = 0;
						// while (remoteFileContents.charAt(differncePosition)
						// ==
						// desiredFileContents.charAt(differncePosition))
						// differncePosition++;
						// log.info("Content differ at position " +
						// differncePosition);
						// log.info("Remote:\n" +
						// remoteFileContents.substring(differncePosition,
						// differncePosition + 64));
						// log.info("Desired:\n" +
						// desiredFileContents.substring(differncePosition,
						// differncePosition + 64));

						// operation.addWarning(this, ALERT_KEY_CONTENTSMISMATCH, "File contents do not match");
						ops.addWarning(this, "File contents do not match");
					}
				}
			}

			if (isUpToDate) {
				log.debug("Up to date: remoteMd5=" + remoteMd5 + ", sourceMd5=" + sourceMd5);
			}

			{
				FilesystemInfo fsInfo = target.getFilesystemInfoFile(filePath);
				if (fsInfo.isSymlink()) {
					// TODO: Follow symlink for perms
					throw new UnsupportedOperationException();

					// target = new FilePath(fsInfo.target);
					// fsInfo = agent.getFilesystemInfoFile(target);
				}

				if (!isOnlyIfNotExists()) {
					validateOwner(fsInfo);
					validateMode(fsInfo);
				}
			}
		}
	}

	// protected InputStream openSourceStream() throws IOException, OpsException {
	// FileObject fileObject = resolveFileObject(getSourceFile());
	// return fileObject.getContent().getInputStream();
	// }
	//
	// @SupportsOperation(operation = "printDifferences")
	// public void printDifferences(Operation operation) throws OpsException, InterruptedException, IOException {
	// InputStream sourceStream = openSourceStream();
	// String desiredFileContents;
	// try {
	// desiredFileContents = IoUtils.readAll(sourceStream);
	// } finally {
	// IoUtils.safeClose(sourceStream);
	// }
	//
	// String remoteFileContents = smartGetServer().getAgent().downloadTextFile(getRemoteFilePath(), false);
	// int differencePosition = findDifferencePosition(desiredFileContents, remoteFileContents);
	// if (differencePosition != -1) {
	// log.info("Files differ at position " + differencePosition);
	// String desiredExtract = desiredFileContents.substring(Math.min(desiredFileContents.length(),
	// differencePosition));
	// if (desiredExtract.length() > EXTRACT_LENGTH) {
	// desiredExtract = desiredExtract.substring(0, EXTRACT_LENGTH);
	// }
	// String remoteExtract = remoteFileContents.substring(Math.min(remoteFileContents.length(), differencePosition));
	// if (remoteExtract.length() > EXTRACT_LENGTH) {
	// remoteExtract = remoteExtract.substring(0, EXTRACT_LENGTH);
	// }
	// log.info("Desired at position: " + desiredExtract);
	// log.info("Remote at position: " + remoteExtract);
	// } else {
	// log.info("Files are the same");
	// }
	//
	// log.info("Remote:\n" + remoteFileContents);
	// log.info("Desired:\n" + desiredFileContents);
	// // log.info("Remote:\n" + remoteFileContents.substring(differncePosition, Math.min(differncePosition + 64,
	// remoteFileContents.length())));
	// // log.info("Desired:\n" + desiredFileContents.substring(differncePosition, Math.min(differncePosition + 64,
	// desiredFileContents.length())));
	//
	// }
	//
	// private static int findDifferencePosition(String left, String right) {
	// int pos = 0;
	// int leftLength = left.length();
	// int rightLength = right.length();
	// while (true) {
	// if (pos >= leftLength || pos >= rightLength) {
	// if (leftLength == rightLength)
	// return -1;
	// return Math.min(leftLength, rightLength);
	// }
	//
	// if (left.charAt(pos) != right.charAt(pos)) {
	// return pos;
	// }
	//
	// pos++;
	// }
	// }
	//
	// public String getSourceFile() {
	// return sourceFile;
	// }
	//
	// public void setSourceFile(String sourceFile) {
	// this.sourceFile = sourceFile;
	// }

	boolean isSymlinkVersions() {
		return false;
	}

	public boolean isOnlyIfNotExists() {
		return onlyIfNotExists;
	}

	public ManagedFile setOnlyIfNotExists(boolean onlyIfNotExists) {
		this.onlyIfNotExists = onlyIfNotExists;
		return this;
	}

	// public boolean isSymlinkVersions() {
	// return symlinkVersions;
	// }
	//
	// public void setSymlinkVersions(boolean symlinkVersions) {
	// this.symlinkVersions = symlinkVersions;
	// }
	//
	// public static ManagedFile build(OpsSystem opsSystem, String sourcePath, FilePath destPath) {
	// String key = NodeUtils.sanitizeKeyName("file-" + destPath);
	// ManagedFile file = new ManagedFile(opsSystem, key, null);
	// file.setSourceFile(sourcePath);
	// file.setRemoteFilePath(destPath);
	// return file;
	// }
}