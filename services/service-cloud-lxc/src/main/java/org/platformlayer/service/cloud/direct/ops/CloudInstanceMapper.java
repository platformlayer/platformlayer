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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class CloudInstanceMapper extends OpsTreeBase implements CustomRecursor {
	private static final Logger log = LoggerFactory.getLogger(CloudInstanceMapper.class);

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
			if (OpsContext.isDelete()) {
				host = null;
			} else {
				if (createInstance) {
					DirectCloudHost cloudHost = cloudMap.pickHost(instance);
					host = cloudHost.getModel();

					platformLayer.addTag(host.getKey(), tag);
				} else {
					throw new OpsException("Instance not yet assigned");
				}
			}
		} else {
			host = hosts.get(0);
		}

		RecursionState recursion = getRecursionState();
		if (host != null) {
			this.cloud = platformLayer.getItem(host.cloud, DirectCloud.class);

			this.hostTarget = directHelpers.toTarget(host);

			recursion.pushChildScope(cloud);
			recursion.pushChildScope(host);
			recursion.pushChildScope(hostTarget);
		} else {
			if (!OpsContext.isDelete()) {
				throw new IllegalStateException();
			}
			log.info("No host set; won't recurse in");

			recursion.setPreventRecursion(true);
		}
	}

	@Override
	protected void addChildren() throws OpsException {
	}
}
