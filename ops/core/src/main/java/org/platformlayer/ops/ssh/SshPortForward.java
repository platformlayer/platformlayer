package org.platformlayer.ops.ssh;

import java.io.Closeable;
import java.net.InetSocketAddress;

public interface SshPortForward extends Closeable {

	InetSocketAddress getLocalSocketAddress();

}
