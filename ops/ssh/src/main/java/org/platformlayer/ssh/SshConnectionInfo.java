package org.platformlayer.ssh;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.KeyPair;

import org.platformlayer.EqualsUtils;
import org.platformlayer.HasIdentityValues;

public class SshConnectionInfo implements HasIdentityValues {
	InetAddress host;
	int port;
	String username;
	KeyPair keyPair;

	public SocketAddress getSocketAddress() {
		return new InetSocketAddress(host, port);
	}

	public InetAddress getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getUsername() {
		return username;
	}

	public SshConnectionInfo(InetAddress host, int port, String username, KeyPair keyPair) {
		super();
		this.host = host;
		this.port = port;
		this.username = username;
		this.keyPair = keyPair;
	}

	@Override
	public int hashCode() {
		return EqualsUtils.computeHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsUtils.equals(this, obj);
	}

	@Override
	public String toString() {
		return "Host: " + host + ", port: " + port + ", username:" + username;
	}

	@Override
	public Object[] getIdentityValues() {
		return new Object[] { host, port, username, keyPair };
	}

}
