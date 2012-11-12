package org.platformlayer.ops.dns;

import java.io.File;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.filesystem.ManagedFilesystemItem;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.packages.AsBlock;
import org.platformlayer.ops.tree.LateBound;
import org.platformlayer.ops.tree.OpsTreeBase;

public class DnsResolver extends OpsTreeBase {
	@Handler
	public void doOperation() throws OpsException {
	}

	@Override
	protected void addChildren() throws OpsException {
		// GCE sets up a DNS server on the host, which we're supposed to use
		// Also, GCE currently uses Ubuntu, which has a more complicated setup for resolv.conf
		addChild(new LateBound<ManagedFilesystemItem>() {
			@Override
			protected ManagedFilesystemItem get() throws OpsException {
				OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);

				AsBlock asBlock = AsBlock.find(target);
				if (asBlock != AsBlock.GOOGLE_COMPUTE_ENGINE) {
					return TemplatedFile.build(Injection.getInstance(DnsResolverModuleBuilder.class),
							new File("/etc/resolv.conf")).setFileMode("644");
				} else {
					return null;
				}
			}

			@Override
			public String getDescription() throws Exception {
				return "DNS resolver configuration";
			}
		});
	}
}
