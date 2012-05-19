package org.platformlayer.service.zookeeper.ops;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;

import com.google.common.base.Objects;

public class Deviations {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(Deviations.class);

	public static void assertEquals(String expected, String actual, String message) {
		// TODO: Make much more structured
		if (!Objects.equal(expected, actual)) {
			OpsContext.get().addWarning(null, message + " Value mismatch: " + actual + " vs " + expected);
		}
	}

	public static void fail(String message, OpsException e) {
		// TODO: Make much more structured
		// TODO: Should we throw here?
		OpsContext.get().addWarning(null, message + " Exception: " + e.toString());
	}

	public static void assertIn(Collection<String> expected, String actual, String message) {
		// TODO: Make much more structured
		if (!expected.contains(actual)) {
			OpsContext.get().addWarning(null, message + " Value not in expected set: " + actual + " vs " + expected);
		}
	}

}
