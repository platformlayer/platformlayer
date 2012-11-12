package org.platformlayer.jobs.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class JobLogLine {
	public static final String TYPE_ENTER_SCOPE = ">";
	public static final String TYPE_EXIT_SCOPE = "<";

	public long timestamp;
	public String message;
	public int level;
	public JobLogExceptionInfo exception;

	public String type;

	public JobLogLine() {
	}

	public JobLogLine(long timestamp, int level, String message, JobLogExceptionInfo exception) {
		this.timestamp = timestamp;
		this.level = level;
		this.message = message;
		this.exception = exception;
	}

	public JobLogExceptionInfo getException() {
		return exception;
	}

	public String getMessage() {
		return message;
	}

	public int getLevel() {
		return level;
	}

	public String getType() {
		return type;
	}

}
