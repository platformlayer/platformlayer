package org.platformlayer.ops.schedule;

import org.platformlayer.RepositoryException;
import org.platformlayer.ops.schedule.jdbc.JdbcSchedulerRepository;

import com.google.inject.ImplementedBy;

@ImplementedBy(JdbcSchedulerRepository.class)
public interface SchedulerRepository {
	SchedulerRecord find(String key) throws RepositoryException;

	void put(SchedulerRecord record) throws RepositoryException;

	Iterable<SchedulerRecord> findAll() throws RepositoryException;

	void logExecution(String key, JobExecution execution, Throwable jobException) throws RepositoryException;
}
