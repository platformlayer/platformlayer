package org.platformlayer.service.cloud.raw.ops;

import java.net.InetAddress;

import javax.inject.Inject;

import org.platformlayer.InetAddressChooser;
import org.platformlayer.core.model.EndpointInfo;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsProvider;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.tagger.Tagger;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.raw.model.RawInstance;
import org.platformlayer.service.cloud.raw.model.RawPublicEndpoint;

public class RawPublicEndpointController extends OpsTreeBase {
	@Inject
	PlatformLayerHelpers platformLayerClient;

	@Inject
	InstanceHelpers instanceHelpers;

	@Override
	protected void addChildren() throws OpsException {
		// We can't actually do anything; we can tag it to mark that the port is open

		final RawPublicEndpoint model = OpsContext.get().getInstance(RawPublicEndpoint.class);

		if (model.publicPort != model.backendPort) {
			throw new OpsException("Port remapping not supported by raw cloud");
		}

		{
			Tagger tagger = injected(Tagger.class);

			OpsProvider<TagChanges> tagChanges = new OpsProvider<TagChanges>() {
				@Override
				public TagChanges get() throws OpsException {
					RawInstance instance = platformLayerClient.getItem(model.instance, RawInstance.class);

					InetAddressChooser chooser = InetAddressChooser.preferIpv4();
					InetAddress publicAddress = chooser.choose(Tag.NETWORK_ADDRESS.find(instance.getTags()));

					if (publicAddress == null) {
						throw new OpsException("Cannot find address for instance: " + model.instance);
					}

					TagChanges tagChanges = new TagChanges();
					EndpointInfo endpoint = new EndpointInfo(publicAddress, model.publicPort);
					tagChanges.addTags.add(endpoint.toTag());

					return tagChanges;
				}
			};
			tagger.platformLayerKey = model.getKey();
			tagger.tagChangesProvider = tagChanges;

			addChild(tagger);
		}
	}

}
