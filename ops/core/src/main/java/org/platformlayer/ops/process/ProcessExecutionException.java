package org.platformlayer.ops.process;

import org.platformlayer.ops.OpsException;

public class ProcessExecutionException extends OpsException {
	private static final long serialVersionUID = 1L;

	private final ProcessExecution execution;

	public ProcessExecutionException() {
		super();
		execution = null;
	}

	public ProcessExecutionException(String message, Throwable cause) {
		super(message, cause);
		execution = null;
	}

	public ProcessExecutionException(String message, ProcessExecution execution) {
		super(message);
		this.execution = execution;
	}

	public ProcessExecutionException(Throwable cause) {
		super(cause);
		execution = null;
	}

	public ProcessExecution getExecution() {
		return execution;
	}

	@Override
	public String getMessage() {
		String message = super.getMessage();
		if (execution != null) {
			message += ": ExitCode=" + execution.getExitCode() + " Stdout=" + execution.getStdOut() + " Stderr="
					+ execution.getStdErr();
		}
		return message;
	}
}
