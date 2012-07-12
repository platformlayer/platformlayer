package org.platformlayer.ops;

import java.io.File;
import java.util.List;

import org.openstack.crypto.Md5Hash;
import org.platformlayer.ops.filesystem.FilesystemInfo;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.process.ProcessExecution;

public interface OpsTarget {
	void doUpload(FileUpload upload) throws OpsException;

	ProcessExecution executeCommand(String command, Object... args) throws OpsException;

	ProcessExecution executeCommand(Command command) throws OpsException;

	File createTempDir() throws OpsException;

	void touchFile(File file) throws OpsException;

	void mkdir(File dir) throws OpsException;

	void mkdir(File dir, String mode) throws OpsException;

	String readTextFile(File file) throws OpsException;

	byte[] readBinaryFile(File file) throws OpsException;

	void chmod(File file, String mode) throws OpsException;

	void chown(File file, String owner, String group, boolean recursive, boolean dereferenceSymlinks)
			throws OpsException;

	void rm(File file) throws OpsException;

	void rmdir(File file) throws OpsException;

	FilesystemInfo getFilesystemInfoFile(File path) throws OpsException;

	List<FilesystemInfo> getFilesystemInfoDir(File path) throws OpsException;

	void mv(File oldName, File newName) throws OpsException;

	void symlink(File targetFile, File aliasFile, boolean force) throws OpsException;

	Md5Hash getFileHash(File filePath) throws OpsException;

	boolean isSameMachine(OpsTarget target);

	NetworkPoint getNetworkPoint();

	boolean isMachineTerminated();
}
