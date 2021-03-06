package org.platformlayer.service.cloud.direct.ops;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.platformlayer.core.model.HostPolicy;
import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.instances.ImageFactory;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.direct.model.DirectInstance;
import org.platformlayer.service.cloud.direct.ops.kvm.KvmInstance;
import org.platformlayer.service.cloud.direct.ops.lxc.LxcInstanceController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.crypto.OpenSshUtils;

public class DirectInstanceController extends OpsTreeBase {
	static final Logger log = LoggerFactory.getLogger(DirectInstanceController.class);

	@Handler
	public void handler() throws OpsException, IOException {
	}

	@Inject
	ImageFactory imageFactory;

	@Bound
	DirectInstance model;

	@Override
	protected void addChildren() throws OpsException {
		HostPolicy hostPolicy = model.hostPolicy;
		if (hostPolicy == null) {
			hostPolicy = new HostPolicy();
		}

		if (hostPolicy.allowRunInContainer) {
			// TODO: The variable initialization probably doesn't belong here
			LxcInstanceController lxc = injected(LxcInstanceController.class);
			String id = model.getId();
			lxc.id = id;
			lxc.instanceDir = new File(DirectCloudUtils.LXC_BASE_DIR, id);

			lxc.minimumMemoryMB = model.minimumMemoryMb;

			addChild(lxc);

			if (model.publicPorts != null) {
				log.info("Ignoring model.publicPorts: " + model.publicPorts);

				// for (int publicPort : model.publicPorts) {
				// PublicPorts publicPortForward = injected(PublicPorts.class);
				// publicPortForward.publicPort = publicPort;
				// publicPortForward.backendPort = publicPort;
				// publicPortForward.backendItem = model;
				// lxc.addChild(publicPortForward);
				// }
			}
		} else {
			// TODO: The variable initialization probably doesn't belong here
			KvmInstance kvm = injected(KvmInstance.class);
			String id = model.getId();
			kvm.id = id;
			kvm.instanceDir = new File(DirectCloudUtils.KVM_BASE_DIR, id);
			kvm.owner = model.getKey();

			kvm.minimumMemoryMB = model.minimumMemoryMb;
			kvm.recipeId = model.recipeId;
			try {
				kvm.sshPublicKey = OpenSshUtils.readSshPublicKey(model.sshPublicKey);
			} catch (IOException e) {
				throw new OpsException("Error deserializing SSH key", e);
			}

			addChild(kvm);

			// TODO: Remove this... it's only supposed to be a hint
			if (model.publicPorts != null) {
				for (int publicPort : model.publicPorts) {
					throw new UnsupportedOperationException();
					//
					// PublicPorts publicPortForward = injected(PublicPorts.class);
					// publicPortForward.publicPort = publicPort;
					// publicPortForward.backendItem = model;
					// kvm.addChild(publicPortForward);
				}
			}
		}
	}

}
