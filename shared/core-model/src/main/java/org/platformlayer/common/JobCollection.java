package org.platformlayer.common;

import java.util.List;

public interface JobCollection {
	List<Job> getJobs();

	void add(Job job);
}
