package org.platformlayer.ops;

import java.io.File;

import org.openstack.crypto.Md5Hash;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.process.ProcessExecution;

/**
 * Creates a target out of machine image that we have to chroot into. Although the default implementations of many
 * OpsTarget might not require overloading many methods, we do overload them so we don't make assumptions about the
 * implementation. e.g. readTextFile likely calls execute('cat {0}'), which would then get chrooted; but if it switched
 * to use scp then this would break.
 * 
 */
public class ChrootOpsTarget extends OpsTargetBase {
	final File chrootDir;
	final File tmpDir;
	final OpsTargetBase parentTarget;

	public ChrootOpsTarget(File chrootDir, File tmpDir, OpsTarget parentTarget) {
		this.chrootDir = chrootDir;
		this.tmpDir = tmpDir;
		this.parentTarget = (OpsTargetBase) parentTarget;
	}

	@Override
	public File createTempDir() throws OpsException {
		// TODO: Auto delete tempdir?
		return createTempDir(tmpDir);
	}

	static File mapToOutsideChroot(File chrootDir, File file) {
		String path = file.getAbsolutePath();
		if (!path.startsWith("/")) {
			throw new IllegalStateException();
		}
		path = path.substring(1);
		return new File(chrootDir, path);
	}

	File mapToOutsideChroot(File file) {
		return mapToOutsideChroot(chrootDir, file);
	}

	@Override
	public void doUpload(FileUpload upload) throws OpsException {
		upload.path = mapToOutsideChroot(upload.path);

		parentTarget.doUpload(upload);
	}

	@Override
	public Md5Hash getFileHash(File path) throws OpsException {
		File innerFile = mapToOutsideChroot(path);
		return parentTarget.getFileHash(innerFile);
	}

	@Override
	protected ProcessExecution executeCommandUnchecked(Command command) throws OpsException {
		// Command innerCommand = Command.build("chroot {0} {1}", chrootDir, command.buildCommandString());

		Command innerCommand = command.prefix("chroot", chrootDir);

		return parentTarget.executeCommand(innerCommand);
	}

	// @Override
	// public File createTempDir() throws OpsException {
	// return inner.createTempDir();
	// }

	@Override
	public void touchFile(File file) throws OpsException {
		parentTarget.touchFile(mapToOutsideChroot(file));
	}

	@Override
	public void mkdir(File dir) throws OpsException {
		parentTarget.mkdir(mapToOutsideChroot(dir));
	}

	@Override
	public String readTextFile(File file) throws OpsException {
		return parentTarget.readTextFile(mapToOutsideChroot(file));
	}

	@Override
	public byte[] readBinaryFile(File file) throws OpsException {
		return parentTarget.readBinaryFile(mapToOutsideChroot(file));
	}

	@Override
	public void chmod(File file, String mode) throws OpsException {
		parentTarget.chmod(mapToOutsideChroot(file), mode);
	}

	@Override
	public void rm(File file) throws OpsException {
		parentTarget.rm(mapToOutsideChroot(file));
	}

	@Override
	public boolean isSameMachine(OpsTarget target) {
		log.warn("isSameMachine stub-implements for ChrootOpsTarget");
		return false;
	}

	@Override
	public NetworkPoint getNetworkPoint() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Command maybeSudo(String command) {
		return parentTarget.maybeSudo(command);
	}

	@Override
	public boolean isMachineTerminated() {
		return parentTarget.isMachineTerminated();
	}
}
