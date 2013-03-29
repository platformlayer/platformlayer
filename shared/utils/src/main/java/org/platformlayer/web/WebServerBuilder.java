package org.platformlayer.web;

import java.io.File;
import java.util.Set;

import org.eclipse.jetty.server.Server;

import com.google.inject.Injector;

public interface WebServerBuilder {

	Server start() throws Exception;

	void addHttpConnector(int port, boolean async);

	void addGuiceContext(String path, Injector injector);

	void addHttpsConnector(int port, Set<SslOption> options) throws Exception;

	void addWar(String key, File file);
}
