package org.platformlayer.jobs.model;

import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class JobLog implements Iterable<JobLogLine> {
	public List<JobLogLine> lines = Lists.newArrayList();

	// Optional
	public JobExecutionData execution;

	public List<JobLogLine> getLines() {
		return lines;
	}

	public void setLines(Iterable<JobLogLine> lines) {
		this.lines.clear();
		for (JobLogLine line : lines) {
			this.lines.add(line);
		}
	}

	@Override
	public Iterator<JobLogLine> iterator() {
		return lines.iterator();
	}

	public JobExecutionData getExecution() {
		return execution;
	}
}
