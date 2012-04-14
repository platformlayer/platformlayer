package org.platformlayer.ssh.mina;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.sshd.SshClient;
import org.apache.sshd.client.keyverifier.DelegatingServerKeyVerifier;
import org.apache.sshd.common.Channel;
import org.apache.sshd.common.Compression;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.compression.CompressionDelayedZlib;
import org.apache.sshd.common.compression.CompressionNone;
import org.apache.sshd.common.compression.CompressionZlib;
import org.platformlayer.ops.ssh.ISshContext;
import org.platformlayer.ops.ssh.SshConnection;
import org.platformlayer.ssh.mina.ciphers.AES128CTR;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class MinaSshContext implements ISshContext {
	// public static final MinaSshContext INSTANCE = new MinaSshContext();

	final SshClient client;

	@Inject
	public MinaSshContext(ExecutorService executorService) {
		this.client = SshClient.setUpDefaultClient();

		// this.client = SshClient.setUpDefaultClient(executorService, false, true);

		// Use compression
		List<NamedFactory<Compression>> compressionFactories = Lists.newArrayList();
		compressionFactories.add(new CompressionZlib.Factory());
		compressionFactories.add(new CompressionDelayedZlib.Factory());
		compressionFactories.add(new CompressionNone.Factory());
		client.setCompressionFactories(compressionFactories);

		// Don't use SSH agent
		client.setChannelFactories(Collections.<NamedFactory<Channel>> emptyList());

		client.setServerKeyVerifier(new DelegatingServerKeyVerifier());

		// Add some better ciphers
		client.getCipherFactories().add(new AES128CTR.Factory());

		client.start();
	}

	public void stop() {
		client.stop();
	}

	@Override
	public SshConnection getSshConnection(String user) {
		MinaSshConnection sshConnection = new MinaSshConnection(this);
		sshConnection.setUser(user);
		return sshConnection;
	}

}
