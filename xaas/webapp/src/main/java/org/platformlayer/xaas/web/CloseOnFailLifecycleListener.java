package org.platformlayer.xaas.web;

import org.slf4j.*;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.component.LifeCycle.Listener;

public class CloseOnFailLifecycleListener implements Listener {

	private static final Logger log = LoggerFactory.getLogger(CloseOnFailLifecycleListener.class);

	@Override
	public void lifeCycleStopping(LifeCycle event) {
		log.info("lifeCycleStopping: " + event);
	}

	@Override
	public void lifeCycleStopped(LifeCycle event) {
		log.info("lifeCycleStopped: " + event);
	}

	@Override
	public void lifeCycleStarting(LifeCycle event) {
		log.info("lifeCycleStarting: " + event);
	}

	@Override
	public void lifeCycleStarted(LifeCycle event) {
		log.info("lifeCycleStarted: " + event);
	}

	@Override
	public void lifeCycleFailure(LifeCycle event, Throwable cause) {
		log.info("lifeCycleFailure: " + event, cause);

		try {
			log.info("Stopping server");
			event.stop();
		} catch (Exception e) {
			log.error("Error while stopping server", e);
			// TODO: System.exit?
		}
	}

}
