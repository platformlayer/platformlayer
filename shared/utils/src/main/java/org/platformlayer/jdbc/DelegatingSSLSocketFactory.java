package org.platformlayer.jdbc;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocketFactory;

public class DelegatingSSLSocketFactory extends SSLSocketFactory {
	final SSLSocketFactory inner;

	protected DelegatingSSLSocketFactory(SSLSocketFactory inner) {
		super();
		this.inner = inner;
	}

	@Override
	public String[] getDefaultCipherSuites() {
		return inner.getDefaultCipherSuites();
	}

	@Override
	public String[] getSupportedCipherSuites() {
		return inner.getSupportedCipherSuites();
	}

	@Override
	public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
		return inner.createSocket(s, host, port, autoClose);

	}

	@Override
	public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
		return inner.createSocket(host, port);
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException,
			UnknownHostException {
		return inner.createSocket(host, port, localHost, localPort);
	}

	@Override
	public Socket createSocket(InetAddress host, int port) throws IOException {
		return inner.createSocket(host, port);
	}

	@Override
	public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
			throws IOException {
		return inner.createSocket(address, port, localAddress, localPort);
	}

}
