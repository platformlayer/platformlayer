package org.platformlayer.metrics;

public interface MetricTimer {

	public interface Context {
		void stop();
	}

	Context start();

}
