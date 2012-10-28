package org.platformlayer.service.cloud.direct.ops;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.platformlayer.TagFilter;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.CustomRecursor;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.direct.model.DirectCloud;
import org.platformlayer.service.cloud.direct.model.DirectHost;
import org.platformlayer.service.cloud.direct.model.DirectInstance;
import org.platformlayer.service.cloud.direct.ops.cloud.CloudMap;
import org.platformlayer.service.cloud.direct.ops.cloud.DirectCloudHost;

import com.google.common.collect.Lists;

public class CloudInstanceMapper extends OpsTreeBase implements CustomRecursor {
	public DirectInstance instance;
	public boolean createInstance = true;

	@Inject
	ServiceContext service;

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	OpsContext ops;

	@Inject
	CloudMap cloudMap;

	// Set during doOperation
	private OpsTarget hostTarget;
	private DirectCloud cloud;

	@Inject
	InstanceHelpers instanceHelpers;

	@Inject
	DirectCloudUtils directHelpers;

	@Handler
	public void doOperation() throws OpsException, IOException {
		Tag tag = Tag.build(Tag.ASSIGNED, instance.getKey().getUrl());
		List<DirectHost> hosts = Lists.newArrayList(platformLayer.listItems(DirectHost.class, TagFilter.byTag(tag)));

		if (hosts.size() > 1) {
			// Huh?
			throw new OpsException("Multiple hosts already assigned");
		}

		DirectHost host;
		if (hosts.isEmpty()) {
			if (createInstance) {
				DirectCloudHost cloudHost = cloudMap.pickHost(instance);
				host = cloudHost.getModel();

				platformLayer.addTag(host.getKey(), tag);
			} else {
				throw new OpsException("Instance not yet assigned");
			}
		} else {
			host = hosts.get(0);
		}

		this.cloud = platformLayer.getItem(host.cloud, DirectCloud.class);

		this.hostTarget = directHelpers.toTarget(host);

		RecursionState recursion = getRecursionState();

		recursion.pushChildScope(cloud);
		recursion.pushChildScope(host);
		recursion.pushChildScope(hostTarget);
	}

	@Override
	protected void addChildren() throws OpsException {
	}
}
