package org.platformlayer.jobs.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class JobDataList /* implements JobCollection */{
	public List<JobData> jobs = Lists.newArrayList();

	public JobDataList() {
	}

	public List<JobData> getJobs() {
		return jobs;
	}

	public static JobDataList concat(Iterable<JobDataList> jobsList) {
		JobDataList ret = new JobDataList();
		for (JobDataList jobs : jobsList) {
			for (JobData job : jobs.getJobs()) {
				ret.jobs.add(job);
			}
		}
		return ret;
	}

	public static JobDataList create() {
		return new JobDataList();
	}

	public void add(JobData job) {
		jobs.add(job);
	}
}
