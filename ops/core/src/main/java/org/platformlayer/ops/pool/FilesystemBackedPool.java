package org.platformlayer.ops.pool;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.filesystem.FilesystemInfo;
import org.platformlayer.ops.process.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.TimeSpan;
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
public abstract class FilesystemBackedPool<T> implements ResourcePool<T> {
	static final Logger log = LoggerFactory.getLogger(FilesystemBackedPool.class);

	final PoolBuilder<T> adapter;
	protected final OpsTarget target;
	final File assignedDir;

	public FilesystemBackedPool(PoolBuilder<T> adapter, OpsTarget target, File assignedDir) {
		this.adapter = adapter;
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

	T pickUnassigned() throws OpsException {
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
				return read(found);
			}

			// TODO: We should implement resource reclamation by checking that symlink targets exist.
			// (We should probably avoid doing this on too many threads concurrently)
			if (i == 0) {
				extendPool();
			}
		}

		return null;
	}

	protected abstract void extendPool() throws OpsException;

	private boolean createSymlink(File symlinkTarget, File link) throws OpsException {
		// symlinkTarget = symlinkTarget.getAbsoluteFile();

		Command command = Command.build("ln -s -T {0} {1}", symlinkTarget, link);
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
	public T findAssigned(PlatformLayerKey owner) throws OpsException {
		int count = 0;

		String expectedTarget = toFile(owner).getAbsolutePath();

		for (FilesystemInfo file : target.getFilesystemInfoDir(assignedDir)) {
			if (!file.isSymlink()) {
				continue;
			}

			count++;

			if (file.symlinkTarget.equals(expectedTarget)) {
				return read(getKey(file));
			}
		}

		if (count == 0) {
			target.mkdir(assignedDir);
		}

		return null;
	}

	private File toFile(PlatformLayerKey owner) {
		// Construct a fake filename to represent the resource
		String name = owner.getUrl();
		name = name.replace(PlatformLayerKey.SCHEME + "://", "/platformlayer/");
		return new File(name);
	}

	@Override
	public T assign(PlatformLayerKey owner, boolean required) throws OpsException {
		T assigned = findAssigned(owner);
		if (assigned != null) {
			return assigned;
		}

		for (int i = 0; i < 10; i++) {
			T unassigned = pickUnassigned();
			if (unassigned == null) {
				break;
			}

			if (createSymlink(toFile(owner), new File(assignedDir, toKey(unassigned)))) {
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
	public void release(PlatformLayerKey owner, T item) throws OpsException {
		File symlink = new File(assignedDir, toKey(item));
		FilesystemInfo info = target.getFilesystemInfoFile(symlink);
		if (info == null) {
			throw new OpsException("Symlink not found");
		}

		if (!Objects.equal(info.symlinkTarget, toFile(owner).getAbsolutePath())) {
			throw new OpsException("Resource not assigned to owner");
		}

		target.rm(symlink);
	}

	protected String toKey(T item) {
		return adapter.toKey(item);
	}

	protected abstract T read(String key) throws OpsException;
}
