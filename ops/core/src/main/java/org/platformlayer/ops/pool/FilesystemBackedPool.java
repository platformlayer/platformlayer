package org.platformlayer.ops.pool;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.platformlayer.TimeSpan;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.filesystem.FilesystemInfo;
import org.platformlayer.ops.process.ProcessExecutionException;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * We use symlinks within a filesystem to implement a really lightweight pool.
 * 
 * There are two directories: resourceDir/ has one file for each controlled resource assignedDir/ has a symlink to
 * another file for each assigned resource
 * 
 * Assigned resources are not removed from resourceDir; rather you have to compare the contents of the two directories.
 * 
 * Symlinks are typically created to a file/directory representing the resource holder. If the file is deleted, the
 * resource is no longer considered assigned.
 * 
 * @author justinsb
 * 
 */
public abstract class FilesystemBackedPool implements ResourcePool {
	static final Logger log = Logger.getLogger(FilesystemBackedPool.class);

	final PoolBuilder poolBuilder;
	protected final OpsTarget target;
	final File assignedDir;

	public FilesystemBackedPool(PoolBuilder poolBuilder, OpsTarget target, File assignedDir) {
		this.poolBuilder = poolBuilder;
		this.target = target;
		this.assignedDir = assignedDir;
	}

	// public static class Assigned {
	// public String key;
	// public Properties properties;
	// }

	protected List<String> list(File dir) throws OpsException {
		List<String> files = Lists.newArrayList();

		for (FilesystemInfo file : target.getFilesystemInfoDir(dir)) {
			String key = getKey(file);
			files.add(key);
		}

		return files;
	}

	private String getKey(FilesystemInfo file) {
		String path = file.name;
		int lastSlash = path.lastIndexOf('/');
		if (lastSlash != -1) {
			path = path.substring(lastSlash + 1);
		}
		return path;
	}

	protected abstract Iterable<String> pickRandomResource() throws OpsException;

	String pickUnassigned() throws OpsException {
		for (int i = 0; i < 2; i++) {
			Set<String> assigned = Sets.newHashSet(list(assignedDir));

			String found = null;
			for (String resource : pickRandomResource()) {
				if (!assigned.contains(resource)) {
					found = resource;
					break;
				}
			}

			if (found != null) {
				return found;
			}

			// TODO: We should implement resource reclamation by checking that symlink targets exist.
			// (We should probably avoid doing this on too many threads concurrently)
			if (i == 0) {
				if (poolBuilder != null) {
					int added = poolBuilder.extendPool(this);
					if (added != 0) {
						log.warn("Added " + added + " items to pool");
					}
				}
			}
		}

		return null;
	}

	private boolean createSymlink(File src, File link) throws OpsException {
		src = src.getAbsoluteFile();

		Command command = Command.build("ln -s -T {0} {1}", src, link);
		try {
			target.executeCommand(command);
			return true;
		} catch (ProcessExecutionException e) {
			if (e.getExecution().getStdErr().endsWith("File exists")) {
				return false;
			}
			throw new OpsException("Error creating symlink", e);
		}
	}

	@Override
	public String findAssigned(File owner) throws OpsException {
		for (FilesystemInfo file : target.getFilesystemInfoDir(assignedDir)) {
			if (!file.isSymlink()) {
				continue;
			}

			if (file.symlinkTarget.equals(owner.getAbsolutePath())) {
				return getKey(file);
			}
		}

		return null;
	}

	@Override
	public String assign(File owner, boolean required) throws OpsException {
		String assigned = findAssigned(owner);
		if (assigned != null) {
			return assigned;
		}

		for (int i = 0; i < 10; i++) {
			String unassigned = pickUnassigned();
			if (unassigned == null) {
				break;
			}

			if (createSymlink(owner, new File(assignedDir, unassigned))) {
				return unassigned;
			}

			if (!TimeSpan.ONE_SECOND.doSafeSleep()) {
				break;
			}
		}

		if (required) {
			throw new OpsException("Unable to assign value from pool: " + toString());
		}
		return null;
	}

	@Override
	public void release(File owner, String item) throws OpsException {
		File symlink = new File(assignedDir, item);
		FilesystemInfo info = target.getFilesystemInfoFile(symlink);
		if (info == null) {
			throw new OpsException("Symlink not found");
		}

		if (!Objects.equal(info.symlinkTarget, owner.getAbsolutePath())) {
			throw new OpsException("Resource not assigned to owner");
		}

		target.rm(symlink);
	}

	@Override
	public abstract Properties readProperties(String key) throws OpsException;
}
