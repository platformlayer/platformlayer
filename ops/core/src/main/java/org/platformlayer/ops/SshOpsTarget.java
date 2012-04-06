package org.platformlayer.ops;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

import org.openstack.utils.Utf8;
import org.platformlayer.ExceptionUtils;
import org.platformlayer.TimeSpan;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.process.ProcessExecution;
import org.platformlayer.ops.process.ProcessExecutionException;
import org.platformlayer.ops.ssh.SshConnection;
import org.platformlayer.ops.ssh.SshException;

import com.google.common.base.Objects;

public class SshOpsTarget extends OpsTargetBase {
    private final SshConnection sshConnection;
    private final File tempDirBase;

    public SshOpsTarget(File tempDirBase, SshConnection sshConnection) {
        this.tempDirBase = tempDirBase;
        this.sshConnection = sshConnection;
    }

    public InetAddress getHost() {
        return sshConnection.getHost();
    }

    @Override
    public File createTempDir() throws OpsException {
        // TODO: Auto delete tempdir?
        return createTempDir(tempDirBase);
    }

    @Override
    public void setFileContents(File path, byte[] contents) throws ProcessExecutionException {
        InputStream srcData = new ByteArrayInputStream(contents);
        try {
            log.info("Uploading file over ssh: " + path);
            sshConnection.sshCopyData(srcData, contents.length, path.getPath(), "0600");
        } catch (IOException e) {
            throw new ProcessExecutionException("Error during file upload", e);
        } catch (InterruptedException e) {
            ExceptionUtils.handleInterrupted(e);
            throw new ProcessExecutionException("Error during file upload", e);
        } catch (SshException e) {
            throw new ProcessExecutionException("Error during file upload", e);
        }
    }

    @Override
    protected ProcessExecution executeCommandUnchecked(Command command) throws ProcessExecutionException {
        try {
            String commandString = command.buildCommandString();
            TimeSpan timeout = command.getTimeout();
            return sshConnection.sshExecute(commandString, timeout);
        } catch (IOException e) {
            throw new ProcessExecutionException("Error during command execution", e);
        } catch (InterruptedException e) {
            ExceptionUtils.handleInterrupted(e);
            throw new ProcessExecutionException("Error during command execution", e);
        } catch (SshException e) {
            throw new ProcessExecutionException("Error during command execution", e);
        }
    }

    @Override
    public String readTextFile(File path) throws OpsException {
        byte[] contents;
        try {
            contents = sshConnection.sshReadFile(path.getPath());
        } catch (IOException e) {
            throw new OpsException("Error reading file", e);
        } catch (InterruptedException e) {
            throw new OpsException("Error reading file", e);
        } catch (SshException e) {
            throw new OpsException("Error reading file", e);
        }
        if (contents == null)
            return null;
        return Utf8.toString(contents);
    }

    @Override
    public boolean isSameMachine(OpsTarget compare) {
        if (compare instanceof SshOpsTarget) {
            InetAddress compareHost = ((SshOpsTarget) compare).sshConnection.getHost();
            InetAddress myHost = sshConnection.getHost();

            return Objects.equal(compareHost, myHost);
        }
        return false;
    }

    @Override
    public NetworkPoint getNetworkPoint() {
        InetAddress myHost = sshConnection.getHost();
        return NetworkPoint.forSameNetwork(myHost);
    }

}
