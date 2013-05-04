package org.platformlayer.web;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.thread.ThreadPool;

import com.fathomdb.server.http.JettyWebServerBuilder;
import com.yammer.metrics.jetty.InstrumentedHandler;
import com.yammer.metrics.jetty.InstrumentedQueuedThreadPool;
import com.yammer.metrics.jetty.InstrumentedSelectChannelConnector;
import com.yammer.metrics.jetty.InstrumentedSocketConnector;

public class InstrumentedJettyWebServerBuilder extends JettyWebServerBuilder {

	// TODO: InstrumentedBlockingChannelConnector

	@Override
	protected Connector buildSocketConnector(int port) {
		return new InstrumentedSocketConnector(port);
	}

	@Override
	protected Connector buildSelectChannelConnector(int port) {
		return new InstrumentedSelectChannelConnector(port);
	}

	@Override
	protected Handler decorateContext(ServletContextHandler context) {
		return new InstrumentedHandler(context);
	}

	@Override
	protected ThreadPool buildThreadPool() {
		return new InstrumentedQueuedThreadPool();
	}

}
