package org.platformlayer.service.cloud.openstack.ops;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsProvider;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.helpers.ImageFactory;
import org.platformlayer.ops.tagger.Tagger;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.openstack.model.OpenstackInstance;
import org.platformlayer.service.cloud.openstack.ops.openstack.OpenstackComputeMachine;

public class OpenstackInstanceController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(OpenstackInstanceController.class);

	@Handler
	public void handler() throws OpsException, IOException {
	}

	@Inject
	ImageFactory imageFactory;

	@Override
	protected void addChildren() throws OpsException {
		final OpenstackInstance model = OpsContext.get().getInstance(OpenstackInstance.class);

		CloudInstanceMapper instance;
		{
			instance = injected(CloudInstanceMapper.class);
			instance.instance = model;
			addChild(instance);
		}

		{
			OpsProvider<TagChanges> tagChanges = new OpsProvider<TagChanges>() {
				@Override
				public TagChanges get() {
					OpenstackComputeMachine machine = OpsContext.get().getInstance(OpenstackComputeMachine.class);

					TagChanges tagChanges = new TagChanges();
					tagChanges.addTags.add(new Tag(Tag.INSTANCE_KEY, OpsSystem.toKey(model).getUrl()));
					tagChanges.addTags.addAll(machine.buildAddressTags());

					return tagChanges;
				}
			};

			instance.addChild(Tagger.build(model, tagChanges));
		}

		// Note: We can't bootstrap an instance, because we can't log in to it,
		// because the public key is not our service's public key

		// if (model.publicPorts != null) {
		// for (int publicPort : model.publicPorts) {
		// PublicPorts publicPortForward = injected(PublicPorts.class);
		// publicPortForward.port = publicPort;
		// publicPortForward.backendItem = model;
		// kvm.addChild(publicPortForward);
		// }
		// }
	}

}
