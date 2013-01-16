package org.platformlayer.service.cloud.openstack.ops;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import org.openstack.client.OpenstackException;
import org.openstack.client.OpenstackNotFoundException;
import org.openstack.client.common.OpenstackComputeClient;
import org.openstack.client.compute.AsyncServerOperation;
import org.openstack.model.compute.SecurityGroup;
import org.openstack.model.compute.Server;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;
import org.platformlayer.ops.CustomRecursor;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.MachineCreationRequest;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.helpers.SshKey;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.openstack.model.OpenstackCloud;
import org.platformlayer.service.cloud.openstack.model.OpenstackInstance;
import org.platformlayer.service.cloud.openstack.ops.openstack.CloudBehaviours;
import org.platformlayer.service.cloud.openstack.ops.openstack.OpenstackCloudContext;
import org.platformlayer.service.cloud.openstack.ops.openstack.OpenstackCloudHelpers;
import org.platformlayer.service.cloud.openstack.ops.openstack.OpenstackComputeMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.crypto.OpenSshUtils;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

public class CloudInstanceMapper extends OpsTreeBase implements CustomRecursor {

	private static final Logger log = LoggerFactory.getLogger(CloudInstanceMapper.class);

	public OpenstackInstance instance;
	public boolean createInstance = true;

	@Inject
	ServiceContext service;

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	OpsContext ops;

	@Inject
	InstanceHelpers instanceHelpers;

	@Inject
	OpenstackCloudContext openstack;

	@Inject
	OpenstackCloudHelpers openstackHelpers;

	@Handler
	public void doOperation() throws OpsException, IOException {
		Tags instanceTags = instance.getTags();

		OpenstackCloud cloud = findCloud();
		if (cloud == null) {
			throw new OpsException("Could not find cloud");
		}
		OpenstackComputeClient computeClient = openstack.getComputeClient(cloud);

		getRecursionState().pushChildScope(cloud);

		List<String> assignedInstanceIds = instanceTags.findAll(Tag.ASSIGNED);
		if (assignedInstanceIds.isEmpty()) {
			if (createInstance && !OpsContext.isDelete()) {
				MachineCreationRequest request = buildMachineCreationRequest();

				PlatformLayerKey instanceKey = instance.getKey();
				request.tags.add(Tag.buildParentTag(instanceKey));

				String serverName = buildServerName();

				Server created = openstack.createInstance(cloud, serverName, request);

				{
					Tag instanceTag = Tag.build(Tag.ASSIGNED, created.getId());
					platformLayer.addTag(instance.getKey(), instanceTag);
				}

				assignedInstanceIds.add(created.getId());
			}
		}

		if (assignedInstanceIds.isEmpty() && !OpsContext.isDelete()) {
			throw new OpsException("Instance not yet assigned");
		}

		Machine machine = null;
		OpsTarget target = null;

		if (!assignedInstanceIds.isEmpty()) {
			if (assignedInstanceIds.size() != 1) {
				log.warn("Multiple instance ids found: " + assignedInstanceIds);
			}

			// We just take the first instance id
			String assignedInstanceId = Iterables.getFirst(assignedInstanceIds, null);

			Server server = openstack.findServerById(cloud, assignedInstanceId);

			if (server == null) {
				if (OpsContext.isConfigure()) {
					throw new OpsException("Unable to find assigned server: " + assignedInstanceId);
				}
			} else {
				server = openstack.ensureHasPublicIp(cloud, server);

				AsyncServerOperation powerOnOperation = openstack.ensurePoweredOn(cloud, server);
				if (powerOnOperation != null) {
					waitOperation(powerOnOperation);
				}

				machine = new OpenstackComputeMachine(openstack, cloud, server);

				SshKey sshKey = service.getSshKey();
				target = machine.getTarget(sshKey);
			}
		}

		if (!assignedInstanceIds.isEmpty() && OpsContext.isDelete()) {
			CloudBehaviours cloudBehaviours = new CloudBehaviours(cloud);
			boolean supportsSecurityGroups = cloudBehaviours.supportsSecurityGroups();

			for (String instanceId : assignedInstanceIds) {
				Server server = openstack.findServerById(cloud, instanceId);
				if (server == null) {
					log.warn("Could not find assigned server: " + instanceId + ", ignoring");
					continue;
				}

				SecurityGroup securityGroup = null;

				if (supportsSecurityGroups) {
					securityGroup = openstackHelpers.getMachineSecurityGroup(computeClient, server);
				}

				AsyncServerOperation terminateOperation = openstack.terminateInstance(cloud, instanceId);

				if (securityGroup != null) {
					// We need to terminate the instance before we delete the security group it uses
					if (terminateOperation != null) {
						waitOperation(terminateOperation);
					}

					try {
						log.info("Deleting security group: " + securityGroup.getId());
						computeClient.root().securityGroups().securityGroup(securityGroup.getId()).delete();
					} catch (OpenstackNotFoundException e) {
						log.info("Ignoring not-found error while deleting security group: " + securityGroup.getId());
					}
				}
			}
		}

		RecursionState recursion = getRecursionState();

		if (OpsContext.isDelete() && machine == null) {
			recursion.setPreventRecursion(true);
		} else {
			recursion.pushChildScope(machine);
			recursion.pushChildScope(target);
		}
	}

	private void waitOperation(AsyncServerOperation operation) throws OpsException {
		try {
			log.info("Waiting for server operation to complete");
			operation.waitComplete(2, TimeUnit.MINUTES);
		} catch (TimeoutException e) {
			throw new OpsException("Timeout waiting for server operation to complete", e);
		} catch (OpenstackException e) {
			throw new OpsException("Error waiting for server operation to complete", e);
		}
	}

	private String buildServerName() {
		String serverName = "PlatformLayer ";
		if (!Strings.isNullOrEmpty(instance.hostname)) {
			serverName += instance.hostname;
		} else {
			serverName += instance.getKey().getUrl();
		}
		return serverName;
	}

	private MachineCreationRequest buildMachineCreationRequest() throws IOException {
		MachineCreationRequest request = new MachineCreationRequest();
		request.sshPublicKey = OpenSshUtils.readSshPublicKey(instance.sshPublicKey);
		request.minimumMemoryMB = instance.minimumMemoryMb;
		request.recipeId = instance.recipeId;
		// request.securityGroups;
		request.hostPolicy = instance.hostPolicy;
		request.hostname = instance.hostname;
		request.publicPorts = instance.publicPorts;

		Tags tags = new Tags();
		request.tags = tags;
		return request;
	}

	private OpenstackCloud findCloud() throws OpsException {
		OpenstackCloud cloud = null;
		{
			cloud = platformLayer.getItem(instance.cloud, OpenstackCloud.class);
		}
		return cloud;
	}

	@Override
	protected void addChildren() throws OpsException {
	}
}
