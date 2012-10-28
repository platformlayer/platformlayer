package org.platformlayer.service.cloud.google.ops;

import java.io.IOException;
import java.security.PublicKey;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.common.Tagset;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;
import org.platformlayer.crypto.OpenSshUtils;
import org.platformlayer.ops.CustomRecursor;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.MachineCreationRequest;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.SshOpsTarget;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.helpers.SshKey;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.google.model.GoogleCloud;
import org.platformlayer.service.cloud.google.model.GoogleCloudInstance;
import org.platformlayer.service.cloud.google.ops.compute.GoogleComputeClient;
import org.platformlayer.service.cloud.google.ops.compute.GoogleComputeClientFactory;
import org.platformlayer.service.cloud.google.ops.compute.GoogleComputeMachine;

import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.Operation;
import com.google.common.collect.Iterables;

public class CloudInstanceMapper extends OpsTreeBase implements CustomRecursor {
	static final Logger log = Logger.getLogger(CloudInstanceMapper.class);

	public GoogleCloudInstance instance;
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
	GoogleComputeClientFactory googleComputeClientFactory;

	@Handler
	public void doOperation() throws OpsException, IOException {
		Tagset instanceTags = instance.getTags();

		GoogleCloud cloud = findCloud();
		if (cloud == null) {
			throw new OpsException("Could not find cloud");
		}
		GoogleComputeClient computeClient = googleComputeClientFactory.getComputeClient(cloud);

		getRecursionState().pushChildScope(cloud);

		List<String> assignedInstanceIds = instanceTags.findAll(Tag.ASSIGNED);
		if (assignedInstanceIds.isEmpty()) {
			if (createInstance && !OpsContext.isDelete()) {
				MachineCreationRequest request = buildMachineCreationRequest();

				PlatformLayerKey instanceKey = instance.getKey();
				request.tags.add(Tag.buildParentTag(instanceKey));

				PublicKey servicePublicKey = service.getSshKey().getKeyPair().getPublic();
				Instance created = computeClient.createInstance(cloud, request, servicePublicKey);

				{
					Tag instanceTag = Tag.build(Tag.ASSIGNED, created.getName());
					platformLayer.addTag(instance.getKey(), instanceTag);
				}

				assignedInstanceIds.add(created.getName());
			}
		}

		if (assignedInstanceIds.isEmpty() && !OpsContext.isDelete()) {
			throw new OpsException("Instance not yet assigned");
		}

		GoogleComputeMachine machine = null;
		OpsTarget target = null;

		if (!assignedInstanceIds.isEmpty()) {
			if (assignedInstanceIds.size() != 1) {
				log.warn("Multiple instance ids found: " + assignedInstanceIds);
			}

			// We just take the first instance id
			String assignedInstanceId = Iterables.getFirst(assignedInstanceIds, null);

			Instance server = computeClient.findInstanceByName(assignedInstanceId);

			if (server == null) {
				if (OpsContext.isConfigure()) {
					throw new OpsException("Unable to find assigned server: " + assignedInstanceId);
				}
			} else {
				server = computeClient.ensureHasPublicIp(server);

				machine = new GoogleComputeMachine(computeClient, cloud, server);

				SshKey sshKey = service.getSshKey();
				target = machine.getTarget(GoogleComputeClient.USER_NAME, sshKey.getKeyPair());

				// We need to use sudo while we set up root access
				((SshOpsTarget) target).setEnsureRunningAsRoot(true);
			}
		}

		if (!assignedInstanceIds.isEmpty() && OpsContext.isDelete()) {
			// CloudBehaviours cloudBehaviours = new CloudBehaviours(cloud);
			// boolean supportsSecurityGroups = cloudBehaviours.supportsSecurityGroups();

			for (String instanceId : assignedInstanceIds) {
				Instance server = computeClient.findInstanceByName(instanceId);
				if (server == null) {
					log.warn("Could not find assigned server: " + instanceId + ", ignoring");
					continue;
				}

				// TODO: Remove associated firewall rules
				log.warn("Deleting firewall rules not yet implemented");

				// SecurityGroup securityGroup = null;
				// if (supportsSecurityGroups) {
				// securityGroup = openstackHelpers.getMachineSecurityGroup(computeClient, server);
				// }

				Operation terminateOperation = computeClient.terminateInstance(instanceId);
				try {
					computeClient.waitComplete(terminateOperation, 5, TimeUnit.MINUTES);
				} catch (TimeoutException e) {
					throw new OpsException("Timeout while waiting for instance termination", e);
				}

				// if (securityGroup != null) {
				// // We need to terminate the instance before we delete the security group it uses
				// if (terminateOperation != null) {
				// waitOperation(terminateOperation);
				// }
				//
				// try {
				// log.info("Deleting security group: " + securityGroup.getId());
				// computeClient.root().securityGroups().securityGroup(securityGroup.getId()).delete();
				// } catch (OpenstackNotFoundException e) {
				// log.info("Ignoring not-found error while deleting security group: " + securityGroup.getId());
				// }
				// }
			}

			if (machine != null) {
				machine.refreshState();
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

	private GoogleCloud findCloud() throws OpsException {
		GoogleCloud cloud = null;
		{
			cloud = platformLayer.getItem(instance.cloud, GoogleCloud.class);
		}
		return cloud;
	}

	@Override
	protected void addChildren() throws OpsException {
	}

}
