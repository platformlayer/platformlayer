package org.platformlayer.service.cloud.direct.ops.cloud;

import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.helpers.SshKey;
import org.platformlayer.ops.helpers.SshKeys;
import org.platformlayer.ops.machines.PlatformLayerCloudHelpers;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.service.cloud.direct.model.DirectHost;
import org.platformlayer.service.cloud.direct.model.DirectInstance;

import com.google.common.collect.Lists;

public class CloudMap {
	@Inject
	ServiceContext service;

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	SshKeys sshKeys;

	@Inject
	InstanceHelpers instances;

	@Inject
	PlatformLayerCloudHelpers cloudHelpers;

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
			SshKey sshKey = service.getSshKey();

			List<DirectCloudHost> hosts = Lists.newArrayList();
			for (DirectHost host : platformLayer.listItems(DirectHost.class)) {
				Machine hostMachine = instances.findMachine(host);
				OpsTarget hostTarget = hostMachine.getTarget(sshKey);
				hosts.add(new DirectCloudHost(host, hostTarget));
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
		// TODO: Make better choices
		Random random = new Random();
		List<DirectCloudHost> hosts = getHosts();
		int index = random.nextInt(hosts.size());
		return hosts.get(index);
	}

	// public ImageStore getImageStore() {
	// return imageStore;
	// }

}
