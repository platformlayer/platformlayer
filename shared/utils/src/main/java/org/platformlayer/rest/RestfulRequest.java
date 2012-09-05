package org.platformlayer.rest;

import javax.net.ssl.KeyManager;

import org.platformlayer.http.SslConfiguration;

public interface RestfulRequest<T> {

	T execute() throws RestClientException;

	SslConfiguration getSslConfiguration();

	void setKeyManager(KeyManager keyManager);

}
