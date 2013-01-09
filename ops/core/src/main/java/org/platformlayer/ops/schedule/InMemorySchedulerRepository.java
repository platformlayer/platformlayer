package org.platformlayer.ops.schedule;

import java.util.Map;

import org.slf4j.*;
import org.platformlayer.RepositoryException;

import com.google.common.collect.Maps;

public class InMemorySchedulerRepository implements SchedulerRepository {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(InMemorySchedulerRepository.class);

	final Map<String, SchedulerRecord> store = Maps.newHashMap();

	@Override
	public SchedulerRecord find(String key) throws RepositoryException {
		SchedulerRecord record = store.get(key);
		return record;
	}

	@Override
	public void put(SchedulerRecord record) throws RepositoryException {
		String key = record.key;
		store.put(key, record);
	}

	@Override
	public Iterable<SchedulerRecord> findAll() throws RepositoryException {
		return store.values();
	}

	@Override
	public void logExecution(String key, JobExecution execution, Throwable jobException) throws RepositoryException {
		log.info("TASK EXECUTED: " + key + " success=" + execution.success);
	}

}
