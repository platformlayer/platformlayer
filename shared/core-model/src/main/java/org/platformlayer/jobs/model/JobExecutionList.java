package org.platformlayer.jobs.model;

import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class JobExecutionList implements Iterable<JobExecutionData> {
	public List<JobExecutionData> runs = Lists.newArrayList();

	public List<JobExecutionData> getRuns() {
		return runs;
	}

	public void setRuns(Iterable<JobExecutionData> runs) {
		this.runs.clear();
		for (JobExecutionData line : runs) {
			this.runs.add(line);
		}
	}

	@Override
	public Iterator<JobExecutionData> iterator() {
		return runs.iterator();
	}

	public static JobExecutionList create(Iterable<JobExecutionData> runs) {
		JobExecutionList ret = create();

		ret.runs.clear();
		for (JobExecutionData line : runs) {
			ret.runs.add(line);
		}

		return ret;
	}

	public static JobExecutionList create() {
		JobExecutionList ret = new JobExecutionList();
		return ret;
	}

	public void add(JobExecutionData jobExecutionData) {
		runs.add(jobExecutionData);
	}
}
