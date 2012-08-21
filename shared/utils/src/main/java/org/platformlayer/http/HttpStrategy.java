package org.platformlayer.http;


public interface HttpStrategy {
	HttpConfiguration buildConfiguration(SslConfiguration sslConfiguration);
}
