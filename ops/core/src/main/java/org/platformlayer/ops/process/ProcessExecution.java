package org.platformlayer.ops.process;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.openstack.utils.Utf8;

public class ProcessExecution implements Serializable {
    private static final long serialVersionUID = 1L;

    static final Logger log = Logger.getLogger(ProcessExecution.class);

    private final int exitCode;
    private String stdOut;
    private String stdErr;
    private final byte[] binaryStdOut;
    private final byte[] binaryStdErr;

    final String command;

    public ProcessExecution(String command, int exitCode, byte[] stdOut, byte[] stdErr) {
        this.command = command;
        this.exitCode = exitCode;
        this.binaryStdOut = stdOut;
        this.binaryStdErr = stdErr;
        this.stdOut = null;
        this.stdErr = null;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getStdOut() {
        if (stdOut == null) {
            stdOut = Utf8.toString(binaryStdOut);
        }
        return stdOut;
    }

    public String getStdErr() {
        if (stdErr == null) {
            stdErr = Utf8.toString(binaryStdErr);
        }
        return stdErr;
    }

    public void checkExitCode() throws ProcessExecutionException {
        if (getExitCode() != 0) {
            log.error("Unexpected return code: " + getExitCode() + " from command " + command);

            logStdOutStdErr();

            throw new ProcessExecutionException("Unexpected return code: " + getExitCode(), this);
        }
    }

    public void logStdOutStdErr() {
        log.info("Process StdOut\n" + this.getStdOut());
        log.info("Process StdErr\n" + this.getStdErr());
    }

    public byte[] getBinaryStdErr() {
        return binaryStdErr;
    }

    public byte[] getBinaryStdOut() {
        return binaryStdOut;
    }

    public long estimateResponseSize() {
        long size = 0;
        if (binaryStdErr != null)
            size += binaryStdErr.length;
        if (binaryStdOut != null)
            size += binaryStdOut.length;
        if (stdOut != null)
            size += stdOut.length();
        if (stdErr != null)
            size += stdErr.length();
        return size;
    }
}
