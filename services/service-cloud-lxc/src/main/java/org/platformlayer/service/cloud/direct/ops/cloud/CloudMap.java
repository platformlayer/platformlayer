package org.platformlayer.service.cloud.direct.ops.cloud;

import java.util.List;

import javax.inject.Inject;

import org.platformlayer.choice.Chooser;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.helpers.SshKeys;
import org.platformlayer.ops.machines.PlatformLayerCloudHelpers;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.service.cloud.direct.model.DirectHost;
import org.platformlayer.service.cloud.direct.model.DirectInstance;
import org.platformlayer.service.cloud.direct.ops.DirectCloudUtils;

import com.google.common.collect.Lists;

public class CloudMap {

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	SshKeys sshKeys;

	@Inject
	InstanceHelpers instances;

	@Inject
	PlatformLayerCloudHelpers cloudHelpers;

	@Inject
	DirectCloudUtils directHelpers;

	// final DirectCloud cloudModel;

	public CloudMap() {
		// this.cloudModel = OpsContext.get().getInstance(DirectCloud.class);
	}

	// public ImageStore getImageStore() throws OpsException {
	// ImageStore imageStore = cloudHelpers.getImageStore(cloudModel);
	//
	// if (imageStore == null) {
	// throw new IllegalArgumentException("Image store not configured");
	// }
	//
	// return imageStore;
	// }

	List<DirectCloudHost> hosts;

	private List<DirectCloudHost> getHosts() throws OpsException {
		if (this.hosts == null) {
			List<DirectCloudHost> hosts = Lists.newArrayList();
			for (DirectHost host : platformLayer.listItems(DirectHost.class)) {
				OpsTarget target = directHelpers.toTarget(host);

				hosts.add(new DirectCloudHost(host, target));
			}
			this.hosts = hosts;
		}

		return this.hosts;
	}

	// // public LxcMachineInfo findMachineByLxcId(String instanceId) throws OpsException {
	// // for (LxcCloudHost host : hosts) {
	// // LxcMachineInfo machine = host.findMachineByLxcId(instanceId);
	// // if (machine != null)
	// // return machine;
	// // }
	// // return null;
	// // }
	//
	// // public Machine findMachine(Tag tag) throws OpsException {
	// // for (LxcCloudHost host : hosts) {
	// // Machine machine = host.findMachine(tag);
	// // if (machine != null)
	// // return machine;
	// // }
	// // return null;
	// // }
	//
	public DirectCloudHost pickHost(DirectInstance model) throws OpsException {
		List<DirectCloudHost> hosts = getHosts();

		Chooser<DirectCloudHost> chooser = buildChooser(model);
		return chooser.choose(hosts);
	}

	private Chooser<DirectCloudHost> buildChooser(DirectInstance model) {
		return ScoreHostPolicy.build(model.hostPolicy, model);
		//
		// if (model.hostPolicy == null || Strings.isNullOrEmpty(model.hostPolicy.groupId)) {
		// // Leverage least-loaded functionality of SpreadChooser
		// return SpreadChooser.build(SpreadChooser.DEFAULT_GROUP);
		// // return RandomChooser.build();
		// } else {
		// return SpreadChooser.build(model.hostPolicy.groupId);
		// }
	}

	// public ImageStore getImageStore() {
	// return imageStore;
	// }

}
