package org.platformlayer.ops.instances;

import org.slf4j.*;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.OpsTargetOperation;
import org.platformlayer.ops.process.ProcessExecution;

public class DetectVirtualization {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(DetectVirtualization.class);

	public enum VirtualizationType {
		Unknown, Lxc
	};

	static class Operation implements OpsTargetOperation<VirtualizationType> {

		@Override
		public VirtualizationType apply(OpsTarget target) throws OpsException {
			// Proc files cannot be scp-ed, because the length is zero
			// byte[] data = target.readBinaryFile(new File("/proc/1/environ"));
			ProcessExecution execution = target.executeCommand("cat /proc/1/environ");

			byte[] data = execution.getBinaryStdOut();

			int start = 0;
			while (start < data.length) {
				int end = start;
				while (end < data.length) {
					if (data[end] == 0) {
						break;
					}
					end++;
				}

				String s = new String(data, start, end - start);

				if (s.startsWith("container=")) {
					if (s.equals("container=lxc")) {
						return VirtualizationType.Lxc;
					}
				}
				start = end + 1;
			}

			return VirtualizationType.Unknown;
		}

		@Override
		public boolean isCacheable() {
			return true;
		}

	}

	static final Operation OP = new Operation();

	public static boolean isLxc(OpsTarget target) throws OpsException {
		VirtualizationType virtualizationType = target.runOperation(OP);
		return virtualizationType == VirtualizationType.Lxc;
	}
}
