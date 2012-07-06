package org.platformlayer.service.cloud.google.ops.compute;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.openstack.client.OpenstackException;
import org.platformlayer.ExceptionUtils;
import org.platformlayer.core.model.Tag;
import org.platformlayer.crypto.OpenSshUtils;
import org.platformlayer.ops.MachineCreationRequest;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.service.cloud.google.model.GoogleCloud;
import org.platformlayer.service.imagefactory.v1.DiskImageRecipe;
import org.platformlayer.service.imagefactory.v1.OperatingSystemRecipe;

import com.google.api.services.compute.Compute;
import com.google.api.services.compute.model.AccessConfig;
import com.google.api.services.compute.model.Firewall;
import com.google.api.services.compute.model.FirewallList;
import com.google.api.services.compute.model.Image;
import com.google.api.services.compute.model.ImageList;
import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.MachineType;
import com.google.api.services.compute.model.MachineTypeList;
import com.google.api.services.compute.model.Metadata;
import com.google.api.services.compute.model.Metadata.Items;
import com.google.api.services.compute.model.NetworkInterface;
import com.google.api.services.compute.model.Operation;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class GoogleComputeClient {
	static final Logger log = Logger.getLogger(GoogleComputeClient.class);

	public static final String ZONE_US_EAST1_A = "us-east1-a";
	public static final String ZONE_US_CENTRAL1_A = "us-central1-a";

	public static final String USER_NAME = "platfomlayer";

	public static final String PROJECTID_GOOGLE = "google";

	final Compute compute;
	final String projectId;

	final PlatformLayerHelpers platformLayerClient;

	public GoogleComputeClient(PlatformLayerHelpers platformLayerClient, Compute compute, String projectId) {
		this.platformLayerClient = platformLayerClient;
		this.compute = compute;
		this.projectId = projectId;
	}

	public List<Firewall> getInstanceFirewallRules(String instanceUrl) throws OpsException {
		List<Firewall> ret = Lists.newArrayList();

		FirewallList firewalls;
		try {
			log.debug("Listing firewall rules");
			firewalls = compute.firewalls().list(projectId).execute();
		} catch (IOException e) {
			throw new OpenstackException("Error listing firewalls", e);
		}

		// TODO: Use filter

		if (firewalls.getItems() != null) {
			for (Firewall firewall : firewalls.getItems()) {
				if (firewall.getTargetTags() != null && firewall.getTargetTags().contains(instanceUrl)) {
					ret.add(firewall);
				}
			}
		}

		return ret;
	}

	public void createFirewallRule(Firewall rule) throws OpsException {
		try {
			log.debug("Inserting firewall rule: " + rule);
			Operation operation = compute.firewalls().insert(projectId, rule).execute();
			waitComplete(operation, 5, TimeUnit.MINUTES);
			// TODO: Check success of operation?
		} catch (IOException e) {
			throw new OpsException("Error creating firewall", e);
		} catch (TimeoutException e) {
			throw new OpsException("Timeout while waiting for firewall creation", e);
		}
	}

	public void deleteFirewallRule(Firewall rule) throws OpsException {
		try {
			log.debug("Deleting firewall rule: " + rule);
			Operation operation = compute.firewalls().delete(projectId, rule.getName()).execute();
			waitComplete(operation, 5, TimeUnit.MINUTES);
			// TODO: Check success of operation?
		} catch (IOException e) {
			throw new OpsException("Error deleting firewall", e);
		} catch (TimeoutException e) {
			throw new OpsException("Timeout while waiting for firewall deletion", e);
		}
	}

	public Operation waitComplete(Operation operation, int duration, TimeUnit unit) throws TimeoutException,
			OpsException {
		long timeoutAt = System.currentTimeMillis() + unit.toMillis(duration);

		// TODO: Timeout?
		while (operation.getStatus().equals("RUNNING")) {
			if (timeoutAt < System.currentTimeMillis()) {
				throw new TimeoutException("Timeout while waiting for operation to complete");
			}
			log.debug("Polling for operation completion: " + operation);
			try {
				operation = compute.operations().get(projectId, operation.getName()).execute();
			} catch (IOException e) {
				throw new OpsException("Error waiting for operation to complete", e);
			}

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new OpsException("Interrupted while waiting for operation to complete", e);
			}
		}

		return operation;
	}

	public static String buildNetworkUrl(String projectId, String networkId) {
		return Compute.DEFAULT_BASE_URL + projectId + "/networks/" + networkId;
	}

	public String buildNetworkUrl(String networkId) {
		return buildNetworkUrl(projectId, networkId);
	}

	public static String buildZoneUrl(String projectId, String zoneId) {
		return Compute.DEFAULT_BASE_URL + projectId + "/zones/" + zoneId;
	}

	public Instance createInstance(GoogleCloud cloud, MachineCreationRequest request, PublicKey sshPublicKey)
			throws OpsException {
		// GoogleComputeClient computeClient = getComputeClient(cloud);

		try {
			Image foundImage = null;

			{
				DiskImageRecipe recipe = null;
				if (request.recipeId != null) {
					recipe = platformLayerClient.getItem(request.recipeId, DiskImageRecipe.class);
				}

				OperatingSystemRecipe operatingSystem = null;
				if (recipe != null) {
					operatingSystem = recipe.getOperatingSystem();
				}

				log.info("Listing images to pick best image");
				Iterable<Image> images = listImages(PROJECTID_GOOGLE);

				// TODO: We need a better solution here!!
				log.warn("Hard coding image names");
				Set<String> imageNames = Sets.newHashSet("ubuntu-12-04-v20120621");
				// Set<String> imageNames = Sets.newHashSet("centos-6-2-v20120621");

				for (Image image : images) {
					if (imageNames.contains(image.getName())) {
						foundImage = image;
						break;
					}
				}

				if (foundImage == null) {
					throw new IllegalArgumentException("Could not find image");
				}
			}

			// GCE requires that the name comply with RFC1035, which I think means a valid DNS
			// For now, just use a UUID, with a pl- prefix so it doesn't start with a number
			// TODO: Fix this!
			String instanceName = "pl-" + UUID.randomUUID().toString();

			Operation createServerOperation;
			{
				Instance create = new Instance();

				create.setName(instanceName);

				create.setZone(buildZoneUrl(projectId, ZONE_US_CENTRAL1_A));

				{
					NetworkInterface networkInterface = new NetworkInterface();
					networkInterface.setNetwork(buildNetworkUrl(projectId, "default"));

					AccessConfig networkAccessConfig = new AccessConfig();
					networkAccessConfig.setType("ONE_TO_ONE_NAT");

					networkInterface.setAccessConfigs(Lists.newArrayList(networkAccessConfig));

					create.setNetworkInterfaces(Lists.newArrayList(networkInterface));
				}

				Metadata metadata = new Metadata();
				metadata.setItems(Lists.<Items> newArrayList());
				create.setMetadata(metadata);

				if (request.tags != null) {
					for (Tag tag : request.tags) {
						Metadata.Items meta = new Metadata.Items();
						meta.setKey(tag.getKey());
						meta.setValue(tag.getValue());
						metadata.getItems().add(meta);
					}
				}

				if (request.sshPublicKey != null) {
					Metadata.Items meta = new Metadata.Items();
					meta.setKey("sshKeys");
					try {
						meta.setValue(USER_NAME + ":" + OpenSshUtils.serialize(sshPublicKey));
					} catch (IOException e) {
						throw new OpsException("Error serializing ssh key", e);
					}

					metadata.getItems().add(meta);
				}

				create.setImage(foundImage.getSelfLink());

				MachineType flavor = getClosestInstanceType(request);
				if (flavor == null) {
					throw new OpsException("Cannot determine machine type for request");
				}
				create.setMachineType(flavor.getSelfLink());

				if (request.securityGroups != null) {
					// TODO: Reimplement if needed
					throw new UnsupportedOperationException();
				}

				// if (createdSecurityGroup != null) {
				// ServerForCreate.SecurityGroup serverSecurityGroup = new ServerForCreate.SecurityGroup();
				// serverSecurityGroup.setName(createdSecurityGroup.getName());
				// create.getSecurityGroups().add(serverSecurityGroup);
				// }

				// create.setConfigDrive(cloudBehaviours.useConfigDrive());

				log.info("Launching new server: " + instanceName);
				try {
					createServerOperation = compute.instances().insert(projectId, create).execute();
				} catch (IOException e) {
					throw new OpsException("Error launching new instance", e);
				}
			}

			log.info("Waiting for server to be ready");
			createServerOperation = waitComplete(createServerOperation, 10, TimeUnit.MINUTES);

			Instance created;

			String stateName = null;
			while (true) {
				created = findInstanceByName(instanceName);

				stateName = created.getStatus();
				log.info("Instance state: " + stateName);

				if (stateName.equals("RUNNING")) {
					break;
				}

				Thread.sleep(1000);
			}

			return created;
		} catch (InterruptedException e) {
			ExceptionUtils.handleInterrupted(e);
			throw new OpsException("Error building server", e);
		} catch (OpenstackException e) {
			throw new OpsException("Error building server", e);
		} catch (TimeoutException e) {
			throw new OpsException("Timeout waiting for server build", e);
		}
	}

	private Iterable<Image> listImages(String projectId) throws OpsException {
		List<Image> ret = Lists.newArrayList();

		ImageList imageList;
		try {
			log.debug("Listing images in project " + projectId);
			imageList = compute.images().list(projectId).execute();
		} catch (IOException e) {
			throw new OpsException("Error listing images", e);
		}
		if (imageList.getItems() != null) {
			ret.addAll(imageList.getItems());
		}

		return ret;
	}

	private Iterable<MachineType> listMachineTypes(String projectId) throws OpsException {
		List<MachineType> ret = Lists.newArrayList();

		MachineTypeList machineTypeList;
		try {
			log.debug("Listing machine types in project " + projectId);
			machineTypeList = compute.machineTypes().list(projectId).execute();
		} catch (IOException e) {
			throw new OpsException("Error listing machine types", e);
		}
		if (machineTypeList.getItems() != null) {
			ret.addAll(machineTypeList.getItems());
		}

		return ret;
	}

	private MachineType getClosestInstanceType(MachineCreationRequest request) throws OpsException {
		log.info("Listing machine types to find best size");

		Iterable<MachineType> flavors = listMachineTypes(projectId);
		List<MachineType> candidates = Lists.newArrayList();
		for (MachineType flavor : flavors) {
			// Ignore the machine types with no ephemeral disks - they're the same price
			// TODO: Is this right?
			if (flavor.getEphemeralDisks() == null || flavor.getEphemeralDisks().isEmpty()) {
				continue;
			}
			int instanceMemoryMB = flavor.getMemoryMb();
			if (request.minimumMemoryMB > instanceMemoryMB) {
				continue;
			}

			candidates.add(flavor);
		}

		MachineType bestFlavor = findCheapest(candidates);
		if (bestFlavor == null) {
			return null;
		}

		return bestFlavor;
	}

	private MachineType findCheapest(List<MachineType> candidates) {
		MachineType bestFlavor = null;
		float bestPrice = Float.MAX_VALUE;

		for (MachineType candidate : candidates) {
			float price = computePrice(candidate);
			if (price < bestPrice) {
				bestFlavor = candidate;
				bestPrice = price;
			}
		}
		return bestFlavor;
	}

	private float computePrice(MachineType flavor) {
		// We compute the per-hour price so we can choose the smallest/cheapest instance size

		float price = 0;

		int cpus = flavor.getGuestCpus() != null ? flavor.getGuestCpus() : 0;
		int memoryMb = flavor.getMemoryMb() != null ? flavor.getMemoryMb() : 0;

		// // RAM is $0.10 / hour / GB
		// price += (0.10 / 1024.0) * ram;

		// // Disk is $0.10 / hour / TB
		// int disk = flavor.getDisk();
		// price += (0.10 / 1024.0) * disk;

		// CPUs are $0.145 / hour / vCPU
		price += 0.145 * cpus;

		return price;
	}

	public static List<String> findPublicIps(Instance instance) {
		List<String> ips = Lists.newArrayList();

		List<NetworkInterface> networkInterfaces = instance.getNetworkInterfaces();
		if (networkInterfaces == null) {
			networkInterfaces = Collections.emptyList();
		}

		for (NetworkInterface networkInterface : networkInterfaces) {
			List<AccessConfig> accessConfigList = networkInterface.getAccessConfigs();
			if (accessConfigList == null) {
				continue;
			}

			for (AccessConfig accessConfig : accessConfigList) {
				if (!Objects.equal(accessConfig.getType(), "ONE_TO_ONE_NAT")) {
					throw new IllegalStateException();
				}

				String natIp = accessConfig.getNatIP();
				if (!Strings.isNullOrEmpty(natIp)) {
					ips.add(natIp);
				}
			}
		}

		return ips;
	}

	public Instance findInstanceByName(String name) throws OpsException {
		try {
			log.debug("Retrieving instance by name: " + name);
			Instance instance = compute.instances().get(projectId, name).execute();
			return instance;
		} catch (IOException e) {
			throw new OpsException("Error getting instance", e);
		}
	}

	public Instance ensureHasPublicIp(Instance instance) throws OpsException {
		List<String> publicIps = findPublicIps(instance);

		if (!publicIps.isEmpty()) {
			return instance;
		}

		throw new UnsupportedOperationException();
		//
		//
		// final OpenstackComputeClient compute = getComputeClient(cloud);
		//
		// log.info("Creating floating IP");
		// FloatingIp floatingIp = compute.root().floatingIps().create();
		//
		// // TODO: Don't abandon the IP e.g. if the attach fails
		// log.info("Attching floating IP " + floatingIp.getIp() + " to " + server.getId());
		// compute.root().servers().server(server.getId()).addFloatingIp(floatingIp.getIp());
		//
		// final String serverId = server.getId();
		//
		// try {
		// server = TimeoutPoll.poll(TimeSpan.FIVE_MINUTES, TimeSpan.TEN_SECONDS, new PollFunction<Server>() {
		// @Override
		// public Server call() throws Exception {
		// log.info("Waiting for floating IP attach; polling server: " + serverId);
		// Server server = compute.root().servers().server(serverId).show();
		//
		// List<Ip> publicIps = helpers.findPublicIps(cloud, server);
		// if (publicIps.isEmpty()) {
		// return null;
		// }
		// return server;
		// }
		// });
		// } catch (TimeoutException e) {
		// throw new OpsException("Timeout while waiting for attached public IP to show up", e);
		// } catch (ExecutionException e) {
		// throw new OpsException("Error while waiting for attached public IP to show up", e);
		// }
		//
		// return server;
	}

	public Operation terminateInstance(String instanceId) throws OpsException {
		try {
			log.debug("Terminating instance: " + instanceId);

			Operation operation = compute.instances().delete(projectId, instanceId).execute();
			return operation;
		} catch (IOException e) {
			throw new OpsException("Error deleting instance", e);
		}
	}

}
