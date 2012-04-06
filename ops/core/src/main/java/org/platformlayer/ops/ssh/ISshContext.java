package org.platformlayer.ops.ssh;

public interface ISshContext {
    SshConnection getSshConnection(String user);
}
