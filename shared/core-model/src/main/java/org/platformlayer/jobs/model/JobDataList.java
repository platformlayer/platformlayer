package org.platformlayer.jobs.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.common.Job;
import org.platformlayer.common.JobCollection;

import com.google.common.collect.Lists;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class JobDataList implements JobCollection {
	public List<Job> jobs = Lists.newArrayList();

	public JobDataList() {
	}

	@Override
	public List<Job> getJobs() {
		return jobs;
	}

	public static JobDataList concat(Iterable<JobCollection> jobsList) {
		JobDataList ret = new JobDataList();
		for (JobCollection jobs : jobsList) {
			ret.jobs.addAll(jobs.getJobs());
		}
		return ret;
	}

	public static JobDataList create() {
		return new JobDataList();
	}

	@Override
	public void add(Job job) {
		jobs.add(job);
	}
}
