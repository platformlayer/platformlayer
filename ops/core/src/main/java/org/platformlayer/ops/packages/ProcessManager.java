package org.platformlayer.ops.packages;

import java.util.List;

import org.slf4j.*;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

public class ProcessManager {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ProcessManager.class);

	public static List<String> findOutOfDataProcesses(OpsTarget server) throws OpsException {
		// grep -l deleted /proc/*/maps
		throw new UnsupportedOperationException();
	}
}
