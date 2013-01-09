package org.platformlayer.service.cloud.openstack.ops;

import java.io.IOException;

import javax.inject.Inject;

import org.platformlayer.core.model.EndpointInfo;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsProvider;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.tagger.Tagger;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.openstack.model.OpenstackInstance;
import org.platformlayer.service.cloud.openstack.model.OpenstackPublicEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class OpenstackPublicEndpointController extends OpsTreeBase {

	private static final Logger log = LoggerFactory.getLogger(OpenstackPublicEndpointController.class);

	@Handler
	public void handler() throws OpsException, IOException {
	}

	@Inject
	PlatformLayerHelpers client;

	// @Inject
	// ImageFactory imageFactory;
	//
	@Override
	protected void addChildren() throws OpsException {
		final OpenstackPublicEndpoint model = OpsContext.get().getInstance(OpenstackPublicEndpoint.class);

		OpenstackInstance instance = client.getItem(model.instance, OpenstackInstance.class);

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
