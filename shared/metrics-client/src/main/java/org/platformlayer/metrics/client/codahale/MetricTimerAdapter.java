package org.platformlayer.metrics.client.codahale;

import org.apache.log4j.Logger;
import org.platformlayer.metrics.MetricTimer;

import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

public class MetricTimerAdapter implements MetricTimer {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(MetricTimerAdapter.class);

	final Timer timer;

	MetricTimerAdapter(Timer timer) {
		super();
		this.timer = timer;
	}

	static class ContextAdapter implements MetricTimer.Context {
		private final TimerContext context;

		public ContextAdapter(TimerContext context) {
			this.context = context;
		}

		@Override
		public void stop() {
			context.stop();
		}
	}

	@Override
	public Context start() {
		return new ContextAdapter(timer.time());
	}
}
