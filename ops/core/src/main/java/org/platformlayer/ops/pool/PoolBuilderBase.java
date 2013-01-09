package org.platformlayer.ops.pool;

import java.util.Properties;
import java.util.Set;

import org.platformlayer.ops.OpsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public abstract class PoolBuilderBase<T> implements PoolBuilder {

	private static final Logger log = LoggerFactory.getLogger(PoolBuilderBase.class);

	protected int batchAddCount = 16;

	@Override
	public int extendPool(FilesystemBackedPool o) throws OpsException {
		StaticFilesystemBackedPool pool = (StaticFilesystemBackedPool) o;

		pool.ensureCreated();

		Set<String> resourceKeys = Sets.newHashSet(pool.listResourceKeys());

		int added = 0;

		for (T item : getItems()) {
			String key = toKey(item);

			if (resourceKeys.contains(key)) {
				continue;
			}

			Properties properties = buildProperties(item);

			if (!pool.addResource(key, properties)) {
				log.warn("Unexpectedly did not add resource: " + key);
			}

			added++;

			if (added >= batchAddCount) {
				break;
			}
		}

		return added;
	}

	protected String toKey(T item) {
		return item.toString();
	}

	protected abstract Iterable<T> getItems();

	protected abstract Properties buildProperties(T item);
}
