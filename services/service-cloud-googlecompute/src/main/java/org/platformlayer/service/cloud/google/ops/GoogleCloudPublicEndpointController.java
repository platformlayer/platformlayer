package org.platformlayer.service.cloud.google.ops;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.EndpointInfo;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsProvider;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.tagger.Tagger;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.google.model.GoogleCloudInstance;
import org.platformlayer.service.cloud.google.model.GoogleCloudPublicEndpoint;

import com.google.common.base.Strings;

public class GoogleCloudPublicEndpointController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(GoogleCloudPublicEndpointController.class);

	@Handler
	public void handler() throws OpsException, IOException {
	}

	@Inject
	PlatformLayerHelpers client;

	@Override
	protected void addChildren() throws OpsException {
		final GoogleCloudPublicEndpoint model = OpsContext.get().getInstance(GoogleCloudPublicEndpoint.class);

		GoogleCloudInstance instance = client.getItem(model.instance, GoogleCloudInstance.class);

		CloudInstanceMapper instanceMapper;
		{
			instanceMapper = injected(CloudInstanceMapper.class);
			instanceMapper.instance = instance;
			addChild(instanceMapper);
		}

		final EnsureFirewallIngress ingress;
		{
			ingress = injected(EnsureFirewallIngress.class);
			ingress.model = model;
			instanceMapper.addChild(ingress);
		}

		{
			OpsProvider<TagChanges> tagChanges = new OpsProvider<TagChanges>() {
				@Override
				public TagChanges get() {
					TagChanges tagChanges = new TagChanges();
					String address = ingress.getPublicAddress();
					if (Strings.isNullOrEmpty(address)) {
						throw new IllegalStateException();
					}

					EndpointInfo endpoint = new EndpointInfo(address, model.publicPort);
					tagChanges.addTags.add(endpoint.toTag());

					return tagChanges;
				}
			};

			Tagger tagger = injected(Tagger.class);

			tagger.platformLayerKey = model.getKey();
			tagger.tagChangesProvider = tagChanges;

			instanceMapper.addChild(tagger);

			Tagger tagInstance = injected(Tagger.class);

			tagInstance.platformLayerKey = null;
			tagInstance.platformLayerKey = model.instance;
			tagInstance.tagChangesProvider = tagChanges;

			instanceMapper.addChild(tagInstance);
		}
	}
}
