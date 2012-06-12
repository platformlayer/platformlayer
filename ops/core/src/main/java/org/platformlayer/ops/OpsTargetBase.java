package org.platformlayer.ops;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.openstack.crypto.Md5Hash;
import org.platformlayer.ops.filesystem.FilesystemInfo;
import org.platformlayer.ops.process.ProcessExecution;
import org.platformlayer.ops.process.ProcessExecutionException;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public abstract class OpsTargetBase implements OpsTarget {
	static final Logger log = Logger.getLogger(OpsTargetBase.class);

	public OpsTargetBase() {
	}

	@Override
	public ProcessExecution executeCommand(String commandLiteral, Object... args) throws OpsException {
		Command command = Command.build(commandLiteral, args);
		return executeCommand(command);
	}

	@Override
	public void touchFile(File file) throws OpsException {
		Command command = Command.build("touch {0}", file);
		executeCommand(command);
	}

	@Override
	public void mv(File oldName, File newName) throws OpsException {
		Command command = Command.build("mv {0} {1}", oldName, newName);
		executeCommand(command);
	}

	@Override
	public Md5Hash getFileHash(File filePath) throws OpsException {
		Command command = Command.build("md5sum {0}", filePath);
		ProcessExecution execution = executeCommandUnchecked(command);

		if (execution.getExitCode() == 1) {
			if (execution.getStdErr().contains("No such file or directory")) {
				return null;
			}
		}

		execution.checkExitCode();
		String stdout = execution.getStdOut();

		// Format is "hash filename"
		String[] items = stdout.split(" ");
		return new Md5Hash(items[0]);
	}

	@Override
	public void chown(File path, String owner, String group, boolean recursive, boolean dereferenceSymlinks)
			throws OpsException {
		if (owner == null) {
			throw new IllegalArgumentException("Owner must be specified for a chown call");
		}

		if (path == null) {
			throw new IllegalArgumentException("path");
		}

		Command command = Command.build("chown");
		if (recursive) {
			command.addLiteral("-R");
		}

		if (dereferenceSymlinks) {
			command.addLiteral("--dereference");
		} else {
			command.addLiteral("--no-dereference");
		}

		String ownerAndGroup = owner;
		if (group != null) {
			ownerAndGroup += ":" + group;
		}

		command.addQuoted(ownerAndGroup);
		command.addFile(path);

		executeCommand(command);
	}

	@Override
	public void rm(File file) throws ProcessExecutionException, OpsException {
		Command command = Command.build("rm -f {0}", file);
		executeCommand(command);
	}

	@Override
	public void rmdir(File file) throws ProcessExecutionException, OpsException {
		Command command = Command.build("rm -rf {0}", file);
		executeCommand(command);
	}

	@Override
	public ProcessExecution executeCommand(Command command) throws OpsException {
		log.info("Executing command: " + command.toString());
		if (command.getEnvironment() != null) {
			String s = Joiner.on(",").join(command.getEnvironment().keys());
			log.info("Environment keys set: " + s);
		}
		ProcessExecution execution = executeCommandUnchecked(command);
		if (execution.getExitCode() != 0) {
			throw new ProcessExecutionException("Unexpected exit code from running command.  Command="
					+ command.toString(), execution);
		}
		return execution;
	}

	protected abstract ProcessExecution executeCommandUnchecked(Command command) throws OpsException;

	protected File createTempDir(File tempDirBase) throws OpsException {
		int maxRetries = 10;

		Random random = new Random();
		for (int i = 1; i <= maxRetries; i++) {
			String randomDirName = Long.toHexString(random.nextLong());
			File tempDir = new File(tempDirBase, randomDirName);
			try {
				executeCommand("mkdir {0}", tempDir);
				return tempDir;
			} catch (ProcessExecutionException e) {
				ProcessExecution execution = e.getExecution();

				if (i < maxRetries && execution != null && execution.getExitCode() == 1
						&& execution.getStdErr().contains("File exists")) {
					// Loop again
				} else {
					throw new OpsException("Error creating directory", e);
				}
			}
		}

		throw new IllegalStateException("Unreachable?");
	}

	@Override
	public void mkdir(File dir) throws OpsException {
		mkdir(dir, null);
	}

	@Override
	public void mkdir(File path, String fileMode) throws OpsException {
		if (path.equals(new File("/"))) {
			log.debug("Skipping mkdir on root (never needed): " + path);
			return;
		}

		Command command = Command.build("mkdir");
		command.addLiteral("-p");
		if (fileMode != null) {
			command.addLiteral("-m");
			command.addQuoted(fileMode);
		}
		command.addFile(path);

		try {
			executeCommand(command);
		} catch (ProcessExecutionException e) {
			throw new OpsException("Error creating directory", e);
		}
	}

	@Override
	public void chmod(File file, String mode) throws OpsException {
		try {
			executeCommand("chmod {0} {1}", mode, file);
		} catch (ProcessExecutionException e) {
			throw new OpsException("Error changing file mode", e);
		}
	}

	@Override
	public FilesystemInfo getFilesystemInfoFile(File path) throws OpsException {
		List<FilesystemInfo> fsInfoList = doFind(path, 0);

		if (fsInfoList == null) {
			return null;
		}
		if (fsInfoList.size() == 0) {
			return null;
		}
		if (fsInfoList.size() == 1) {
			return fsInfoList.get(0);
		}
		throw new IllegalStateException("Multiple results found");
	}

	@Override
	public List<FilesystemInfo> getFilesystemInfoDir(File path) throws OpsException {
		List<FilesystemInfo> fsInfoList = doFind(path, 1);
		if (fsInfoList == null) {
			return Collections.emptyList();
		}
		List<FilesystemInfo> ret = Lists.newArrayList();
		for (FilesystemInfo fsInfo : fsInfoList) {
			if (fsInfo.depth == 0) {
				continue;
			}
			ret.add(fsInfo);
		}
		return ret;
	}

	protected List<FilesystemInfo> doFind(File path, Integer maxDepth) throws OpsException {
		Command command = Command.build("find");

		String[] fields = new String[] { "T@", "s", "m", "u", "g", "n", "p", "l", "y", "d" };

		StringBuilder format = new StringBuilder();
		for (int i = 0; i < fields.length; i++) {
			if (i != 0) {
				format.append("\\t");
			}
			format.append("%");
			format.append(fields[i]);
		}
		format.append("\\n");

		command.addFile(path);

		if (maxDepth != null) {
			command.addLiteral("-maxdepth");
			command.addQuoted(maxDepth.toString());
		}

		command.addLiteral("-printf");
		command.addQuoted(format.toString());

		ProcessExecution execution;
		try {
			execution = executeCommand(command);
		} catch (ProcessExecutionException e) {
			execution = e.getExecution();
			if (execution != null && execution.getExitCode() == 1
					&& execution.getStdErr().contains("No such file or directory")) {
				return null;
			}

			throw new OpsException("Error executing find command", e);
		}
		List<FilesystemInfo> filesystemInfos = Lists.newArrayList();

		String stdout = execution.getStdOut();
		for (String line : stdout.split("\n")) {
			String[] fieldValues = line.split("\t");
			if (fieldValues.length != fields.length) {
				throw new OpsException("Cannot parse line: " + line);
			}

			FilesystemInfo filesystemInfo = new FilesystemInfo();

			for (int i = 0; i < fieldValues.length; i++) {
				String field = fields[i];
				String fieldValue = fieldValues[i];
				if (field.equals("u")) {
					filesystemInfo.owner = fieldValue;
				} else if (field.equals("g")) {
					filesystemInfo.group = fieldValue;
				} else if (field.equals("n")) {
					filesystemInfo.links = fieldValue;
				} else if (field.equals("m")) {
					filesystemInfo.mode = fieldValue;
				} else if (field.equals("p")) {
					filesystemInfo.name = fieldValue;
				} else if (field.equals("s")) {
					filesystemInfo.size = Long.parseLong(fieldValue);
				} else if (field.equals("y")) {
					filesystemInfo.type = fieldValue;
				} else if (field.equals("l")) {
					if (!Strings.isNullOrEmpty(fieldValue)) {
						filesystemInfo.symlinkTarget = fieldValue;
					}
				} else if (field.equals("T@")) {
					filesystemInfo.date = fieldValue;
				} else if (field.equals("d")) {
					filesystemInfo.depth = Integer.parseInt(fieldValue);
				} else {
					throw new IllegalStateException();
				}
			}

			filesystemInfos.add(filesystemInfo);
		}

		return filesystemInfos;
	}

	@Override
	public void symlink(File targetFile, File aliasFile, boolean force) throws OpsException {
		Command command = Command.build("ln");
		command.addLiteral("--symbolic");
		if (force) {
			command.addLiteral("--force");
		}
		command.addLiteral("--no-dereference");
		command.addFile(targetFile);
		command.addFile(aliasFile);
		executeCommand(command);
	}
}
