package org.platformlayer.ops.pool;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.platformlayer.ops.FileUpload;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class StaticFilesystemBackedPool<T> extends FilesystemBackedPool<T> {
	protected final File resourceDir;
	private final Class<T> clazz;

	public StaticFilesystemBackedPool(Class<T> clazz, PoolBuilder<T> adapter, OpsTarget target, File resourceDir,
			File assignedDir) {
		super(adapter, target, assignedDir);
		this.clazz = clazz;
		this.resourceDir = resourceDir;
	}

	@Override
	protected Iterable<String> pickRandomResource() throws OpsException {
		List<String> resources = listResourceKeys();
		Collections.shuffle(resources);
		return resources;
	}

	public List<String> listResourceKeys() throws OpsException {
		List<String> resources = Lists.newArrayList(list(resourceDir));
		return resources;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":" + resourceDir;
	}

	public void ensureCreated() throws OpsException {
		target.mkdir(resourceDir);
		target.mkdir(assignedDir);
	}

	private boolean addResource(String key) throws OpsException {
		// JaxbHelper jaxb = JaxbHelper.get(item.getClass());
		//
		// String xml;
		// try {
		// xml = jaxb.marshal(item, true);
		// } catch (JAXBException e) {
		// throw new OpsException("Error serializing as XML", e);
		// }

		String contents = "";
		File path = new File(resourceDir, key);
		if (target.getFilesystemInfoFile(path) != null) {
			return false;
		}

		FileUpload.upload(target, path, contents);

		return true;
	}

	@Override
	protected T read(String key) throws OpsException {
		return adapter.toItem(key);
		//
		// JaxbHelper jaxb = JaxbHelper.get(clazz);
		//
		// String xml;
		// try {
		// File path = new File(resourceDir, key);
		// xml = target.readTextFile(path);
		// if (xml == null) {
		// return null;
		// }
		//
		// return (T) jaxb.unmarshal(xml);
		// } catch (JAXBException e) {
		// throw new OpsException("Error reading XML from pool", e);
		// }
	}

	protected int batchAddCount = 16;

	@Override
	protected void extendPool() throws OpsException {
		ensureCreated();

		Set<String> resourceKeys = Sets.newHashSet(listResourceKeys());

		int added = 0;

		for (String key : adapter.getItems()) {
			if (resourceKeys.contains(key)) {
				continue;
			}

			// Properties properties = buildProperties(item);

			if (!addResource(key)) {
				// Presumably already exists
				log.warn("Unexpectedly did not add resource: " + key);
				continue;
			}

			added++;

			if (added >= batchAddCount) {
				break;
			}
		}

		if (added != 0) {
			log.info("Added " + added + " items to pool");
		} else {
			log.warn("Adapter did not add any items to pool");
		}
	}

}
