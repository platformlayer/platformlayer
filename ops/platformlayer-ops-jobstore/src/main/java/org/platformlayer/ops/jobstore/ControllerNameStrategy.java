package org.platformlayer.ops.jobstore;

import org.platformlayer.ops.HasDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class ControllerNameStrategy {

	private static final Logger log = LoggerFactory.getLogger(ControllerNameStrategy.class);

	public static String getName(Object controller) {
		String name = null;

		if (controller instanceof HasDescription) {
			try {
				name = ((HasDescription) controller).getDescription();
			} catch (Exception e) {
				log.warn("Error while invoking getDescription", e);
				name = null;
			}
		}

		if (Strings.isNullOrEmpty(name)) {
			name = controller.getClass().getSimpleName();
		}

		return name;
	}
}
