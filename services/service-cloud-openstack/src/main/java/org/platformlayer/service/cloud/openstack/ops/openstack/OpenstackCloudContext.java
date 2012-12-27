package org.platformlayer.service.cloud.openstack.ops.openstack;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.openstack.client.OpenstackException;
import org.openstack.client.OpenstackNotFoundException;
import org.openstack.client.common.OpenstackComputeClient;
import org.openstack.client.common.OpenstackImageClient;
import org.openstack.client.compute.AsyncServerOperation;
import org.openstack.model.compute.Addresses.Network.Ip;
import org.openstack.model.compute.CreateSecurityGroupRuleRequest;
import org.openstack.model.compute.Flavor;
import org.openstack.model.compute.FloatingIp;
import org.openstack.model.compute.Image;
import org.openstack.model.compute.KeyPair;
import org.openstack.model.compute.Metadata;
import org.openstack.model.compute.SecurityGroup;
import org.openstack.model.compute.SecurityGroupRule;
import org.openstack.model.compute.Server;
import org.openstack.model.compute.ServerForCreate;
import com.fathomdb.Utf8;
import org.platformlayer.Comparisons;
import org.platformlayer.ExceptionUtils;
import org.platformlayer.TimeSpan;
import org.platformlayer.TimeoutPoll;
import org.platformlayer.TimeoutPoll.PollFunction;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.MachineCreationRequest;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.ImageFactory;
import org.platformlayer.ops.helpers.SshKeys;
import org.platformlayer.ops.images.CloudImage;
import org.platformlayer.ops.images.ImageFormat;
import org.platformlayer.ops.images.ImageStore;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.service.cloud.openstack.model.OpenstackCloud;
import org.platformlayer.service.imagefactory.v1.DiskImageRecipe;
import org.platformlayer.service.imagefactory.v1.OperatingSystemRecipe;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class OpenstackCloudContext {
	// implements CloudContext {
	// }
	static final Logger log = Logger.getLogger(OpenstackCloudContext.class);

	static final String SECURITY_GROUP_PREFIX = "pl__";

	// OpenstackComputeClient openstackComputeClient;

	// public void validate() throws OpsException {
	// OpenstackComputeClient openstackComputeClient = buildOpenstackClient();
	// // Dummy call to validate information
	// try {
	// openstackComputeClient.listLimits();
	// } catch (OpenstackException e) {
	// throw new OpsException("Unable to connect to OpenStack Compute API", e);
	// }
	//
	// OpsContext opsContext = OpsContext.get();
	// OpenstackImageClient openstackImageClient = opsContext.getUserInfo().getOpenstackImageClient();
	// // Dummy call to validate information
	// try {
	// // TODO: Make this call more lightweight
	// openstackImageClient.listImages(false);
	// } catch (OpenstackException e) {
	// throw new OpsException("Unable to connect to OpenStack Image API", e);
	// }
	//
	// }

	// private OpenstackComputeConfiguration getOpenstackComputeConfiguration(UserInfo userInfo) throws
	// OpsConfigException {
	// // ComputeConfiguration computeConfig = new ComputeConfiguration();
	// // computeConfig.endpoint = config.getRequiredString("compute.url");
	// //
	// // String username = config.getRequiredString("compute.username");
	// // String secret = config.getRequiredString("compute.secret");
	// //
	// // computeConfig.awsCredentials = new BasicAWSCredentials(username, secret);
	// // return computeConfig;
	//
	// OpsConfig config = userInfo.getConfig();
	//
	// String url = config.getRequiredString("compute.os.auth");
	//
	// String username = config.getRequiredString("compute.os.username");
	// String secret = config.getRequiredString("compute.os.secret");
	// return new OpenstackComputeConfiguration(url, username, secret);
	// }

	// private OpenstackImageClient openstackImageClient;

	// public OpenstackAuthenticationClient getOpenstackAuthenticationClient(OpenstackCloud cloud) throws OpsException {
	// try {
	// OpenstackClient openstackClient = getOpenstackClient(cloud);
	// return OpenstackAuthenticationClient.loginUsingConfiguration(openstackClient);
	// } catch (OpenstackException e) {
	// throw new OpsException("Error connecting to OpenStack authentication API", e);
	// }
	// }

	public OpenstackComputeClient getComputeClient(OpenstackCloud cloud) throws OpsException {
		// OpsContext opsContext = OpsContext.get();
		//
		// OpenstackComputeClient computeClient = getOpenstackComputeClient(opsContext.getUserInfo());

		OpenstackCloudHelpers helpers = new OpenstackCloudHelpers();
		OpenstackComputeClient computeClient = helpers.buildOpenstackComputeClient(cloud);

		return computeClient;
	}

	// private AmazonEC2Client buildEc2Client() throws OpsException {
	// OpsContext opsContext = OpsContext.get();
	//
	// AmazonEC2Client amazonEC2Client = opsContext.getUserInfo().buildEc2Client();
	//
	// return amazonEC2Client;
	// }
	//
	// // // sshKeyPair = cloud.generateSshKeyPair(sshKeyName);
	// // public KeyPair generateSshKeyPair(String sshKeyName) throws OpsException {
	// // KeyPair sshKeyPair;
	// //
	// // try {
	// // // OpenStack EC2 api does not support import
	// // // // We create a new keypair each time... we don't want the user
	// // // logging in
	// // // KeyPair sshKeyPair = generateKeyPair("RSA", null);
	// //
	// // // ImportKeyPairRequest importKeyPairRequest = new
	// // // ImportKeyPairRequest();
	// // // importKeyPairRequest.setPublicKeyMaterial(publicKeyMaterial);
	// // // ImportKeyPairResult importKeyPairResult =
	// // // computeClient.importKeyPair(importKeyPairRequest);
	// // // sshKeyName = importKeyPairResult.getKeyName();
	// // AmazonEC2Client ec2Api = buildEc2Client();
	// //
	// // CreateKeyPairRequest createKeyPairRequest = new CreateKeyPairRequest(sshKeyName);
	// // CreateKeyPairResult createKeyPairResult = ec2Api.createKeyPair(createKeyPairRequest);
	// //
	// // sshKeyPair = extractKeyPair(createKeyPairResult);
	// //
	// // return sshKeyPair;
	// // } catch (AmazonClientException e) {
	// // throw new OpsException("Error building server", e);
	// // }
	// // }
	//

	public Server findServerById(OpenstackCloud cloud, String serverId) throws OpsException {
		OpenstackComputeClient computeClient = getComputeClient(cloud);

		try {
			log.info("Getting server info for: " + serverId);
			Server server = computeClient.root().servers().server(serverId).show();
			return server;
		} catch (OpenstackNotFoundException e) {
			return null;
		} catch (OpenstackException e) {
			throw new OpsException("Error getting server", e);
		}
	}

	// public OpenstackComputeMachine findMachineByInstanceKey(String instanceId) throws OpsException {
	// OpenstackComputeClient osApi = buildOpenstackClient();
	//
	// try {
	// Server server = osApi.getServerDetails(instanceId);
	// return new OpenstackComputeMachine(getSshIpAddress(server), this, server.getId());
	// } catch (OpenstackException e) {
	// throw new OpsException("Error listing servers", e);
	// }
	// }
	//
	// private String getSshIpAddress(Server server) {
	// Addresses addresses = server.getAddresses();
	// if (addresses != null) {
	// for (Network network : addresses.getNetwork()) {
	// List<Ip> ips = network.getIp();
	// if (ips == null)
	// continue;
	// for (Ip ip : ips) {
	// return ip.getAddr();
	// }
	// }
	// }
	// throw new IllegalArgumentException("Cannot determine ip address for server: " + server.getId());
	// }
	//
	// public OpenstackComputeMachine findMachine(Tag tag) throws OpsException {
	// OpenstackComputeClient osApi = buildOpenstackClient();
	//
	// try {
	// Servers servers = osApi.listServers(true);
	// for (Server server : servers.getServer()) {
	// Metadata metadata = server.getMetadata();
	// if (metadata != null) {
	// for (Meta meta : metadata.getMeta()) {
	// if (Objects.equal(tag.getKey(), meta.getKey())) {
	// if (Objects.equal(tag.getValue(), meta.getContent())) {
	// return new OpenstackComputeMachine(getSshIpAddress(server), this, server.getId());
	// }
	// }
	// }
	// }
	// }
	// return null;
	// } catch (OpenstackException e) {
	// throw new OpsException("Error listing servers", e);
	// }
	// }

	@Inject
	ImageFactory imageFactory;

	@Inject
	PlatformLayerHelpers platformLayerClient;

	public Server createInstance(OpenstackCloud cloud, String serverName, MachineCreationRequest request)
			throws OpsException {
		OpenstackComputeClient computeClient = getComputeClient(cloud);

		try {
			Image foundImage = null;

			CloudBehaviours cloudBehaviours = new CloudBehaviours(cloud);
			if (!cloudBehaviours.canUploadImages()) {
				// For now, we presume this is the HP cloud and hard-code the name
				// if (!cloudBehaviours.isHpCloud()) {
				// throw new UnsupportedOperationException();
				// }

				DiskImageRecipe recipe = null;
				if (request.recipeId != null) {
					recipe = platformLayerClient.getItem(request.recipeId, DiskImageRecipe.class);
				}

				OperatingSystemRecipe operatingSystem = null;
				if (recipe != null) {
					operatingSystem = recipe.getOperatingSystem();
				}

				log.info("Listing images to pick best image");
				Iterable<Image> images = computeClient.root().images().list();
				if (cloudBehaviours.isHpCloud()) {
					// TODO: We need a better solution here!!
					Set<String> imageNames = Sets.newHashSet("Debian Squeeze 6.0.3 Server 64-bit 20120123");
					log.warn("Hard coding image name (presuming HP cloud)");

					// TODO: Match OS
					for (Image image : images) {
						if (imageNames.contains(image.getName())) {
							foundImage = image;
							break;
						}
					}
				} else if (cloudBehaviours.isRackspaceCloud()) {
					if (operatingSystem == null) {
						operatingSystem = new OperatingSystemRecipe();
						operatingSystem.setDistribution("debian");
						operatingSystem.setVersion("squeeze");
					}

					for (Image image : images) {
						boolean matchesDistribution = false;
						boolean matchesVersion = false;

						for (Image.ImageMetadata.ImageMetadataItem item : image.getMetadata()) {
							// if (item.getKey().equals("platformlayer.org__type")) {
							// if (item.getValue().equals("base")) {
							// isMatch = true;
							// }
							// }

							if (item.getKey().equals("os_distro")) {
								if (operatingSystem != null && operatingSystem.getDistribution() != null) {
									if (Comparisons
											.equalsIgnoreCase(operatingSystem.getDistribution(), item.getValue())) {
										matchesDistribution = true;
									}
								}
							}

							if (item.getKey().equals("os_version")) {
								if (operatingSystem != null && operatingSystem.getVersion() != null) {
									if (Comparisons.equalsIgnoreCase(operatingSystem.getVersion(), item.getValue())) {
										matchesVersion = true;
									} else if (Comparisons
											.equalsIgnoreCase(operatingSystem.getDistribution(), "debian")) {

										// Lenny is no longer getting security updates
										// if (Strings.equalsIgnoreCase(operatingSystem.getVersion(), "lenny") &&
										// Strings.equalsIgnoreCase(item.getValue(), "5")) {
										// matchesVersion = true;
										// } else

										if (Comparisons.equalsIgnoreCase(operatingSystem.getVersion(), "squeeze")
												&& Comparisons.equalsIgnoreCase(item.getValue(), "6")) {
											matchesVersion = true;
										} else {
											matchesVersion = false;
										}
									} else if (Comparisons
											.equalsIgnoreCase(operatingSystem.getDistribution(), "ubuntu")) {
										if (Comparisons.equalsIgnoreCase(operatingSystem.getVersion(), "lucid")
												&& Comparisons.equalsIgnoreCase(item.getValue(), "10.04LTS")) {
											matchesVersion = true;
										} else {
											matchesVersion = false;
										}
									} else {
										matchesVersion = false;
									}
								}
							}
						}

						if (matchesDistribution && matchesVersion) {
							foundImage = image;
							break;
						}
					}
				} else {
					for (Image image : images) {
						boolean isMatch = false;

						for (Image.ImageMetadata.ImageMetadataItem item : image.getMetadata()) {
							// if (item.getKey().equals(Tag.IMAGE_TYPE)) {
							// if (item.getValue().equals("base")) {
							// isMatch = true;
							// }
							// }

							if (item.getKey().equals(Tag.IMAGE_OS_DISTRIBUTION)) {
								if (operatingSystem != null && operatingSystem.getDistribution() != null) {
									if (!Comparisons.equalsIgnoreCase(operatingSystem.getDistribution(),
											item.getValue())) {
										isMatch = false;
									}
								}
							}

							if (item.getKey().equals(Tag.IMAGE_OS_VERSION)) {
								if (operatingSystem != null && operatingSystem.getVersion() != null) {
									if (!Comparisons.equalsIgnoreCase(operatingSystem.getVersion(), item.getValue())) {
										isMatch = false;
									}
								}
							}
						}

						if (isMatch) {
							foundImage = image;
							break;
						}
					}
				}

				if (foundImage == null) {
					throw new IllegalArgumentException("Could not find image");
				}
			} else {
				List<ImageFormat> formats = Collections.singletonList(ImageFormat.DiskQcow2);
				CloudImage image = imageFactory.getOrCreateImageId(cloud, formats, request.recipeId);

				String imageId = image.getId();
				log.info("Getting image details for image: " + imageId);
				foundImage = computeClient.root().images().image(imageId).show();
				if (foundImage == null) {
					throw new IllegalArgumentException("Could not find image: " + imageId);
				}
			}

			SecurityGroup createdSecurityGroup = null;
			if (cloudBehaviours.supportsSecurityGroups()) {
				SecurityGroup createTemplate = new SecurityGroup();
				createTemplate.setName(SECURITY_GROUP_PREFIX + serverName);
				createTemplate.setDescription("Security group for instance: " + serverName);
				try {
					log.info("Creating security group: " + createTemplate.getName());
					createdSecurityGroup = computeClient.root().securityGroups().create(createTemplate);
				} catch (OpenstackException e) {
					for (SecurityGroup candidate : computeClient.root().securityGroups().list()) {
						if (Objects.equal(candidate.getName(), createTemplate.getName())) {
							createdSecurityGroup = candidate;
							break;
						}
					}

					if (createdSecurityGroup != null) {
						// Ignore
						log.warn("Ignoring 'security group already exists' error: " + e.getMessage());
					} else {
						throw new OpsException("Error creating security group", e);
					}
				}
				{
					CreateSecurityGroupRuleRequest newRule = new CreateSecurityGroupRuleRequest();
					newRule.setCidr("0.0.0.0/0");
					newRule.setFromPort(22);
					newRule.setToPort(22);
					newRule.setIpProtocol("tcp");
					newRule.setParentGroupId(createdSecurityGroup.getId());

					try {
						log.info("Creating security group rule for port: " + newRule.getToPort());
						SecurityGroupRule createdRule = computeClient.root().securityGroupRules().create(newRule);
					} catch (OpenstackException e) {
						String message = e.getMessage();
						if (message != null && message.contains("This rule already exists")) {
							log.warn("Ignoring 'rule already exists': " + e.getMessage());
						} else {
							throw new OpsException("Error creating security group access", e);
						}
					}
				}
			}

			AsyncServerOperation createServerOperation;
			{
				ServerForCreate create = new ServerForCreate();

				create.setName(serverName);

				if (request.sshPublicKey != null) {
					if (cloudBehaviours.supportsPublicKeys()) {
						OpenstackCloudHelpers cloudHelpers = new OpenstackCloudHelpers();
						KeyPair keyPair = cloudHelpers.ensurePublicKeyUploaded(computeClient, request.sshPublicKeyName,
								request.sshPublicKey);
						create.setKeyName(keyPair.getName());
					} else if (cloudBehaviours.supportsFileInjection()) {
						String fileContents = SshKeys.serialize(request.sshPublicKey);
						create.addUploadFile("/root/.ssh/authorized_keys", Utf8.getBytes(fileContents));
					} else {
						throw new OpsException("No supported SSH key mechanism on cloud");
					}
				}

				create.setImageRef(foundImage.getId());

				Flavor flavor = getClosestInstanceType(computeClient, request);
				if (flavor == null) {
					throw new OpsException("Cannot determine instance type for request");
				}
				create.setFlavorRef(flavor.getId());

				if (request.securityGroups != null) {
					// TODO: Reimplement if needed
					throw new UnsupportedOperationException();
				}

				if (createdSecurityGroup != null) {
					ServerForCreate.SecurityGroup serverSecurityGroup = new ServerForCreate.SecurityGroup();
					serverSecurityGroup.setName(createdSecurityGroup.getName());
					create.getSecurityGroups().add(serverSecurityGroup);
				}

				create.setConfigDrive(cloudBehaviours.useConfigDrive());

				log.info("Launching new server: " + create.getName());
				createServerOperation = computeClient.createServer(create);
			}

			log.info("Waiting for server to be ready");
			Server server = createServerOperation.waitComplete();
			Server instanceInfo = null;
			String stateName = null;
			while (true) {
				instanceInfo = getInstanceInfo(computeClient, server.getId());

				stateName = instanceInfo.getStatus();
				log.info("Instance state: " + stateName);

				// if (stateName.equals("scheduling")) {
				// continue;
				// }
				//
				if (stateName.equals("BUILD")) {
					break;
				}

				// if (stateName.equals("shutdown")) {
				// break;
				// }

				if (stateName.equals("ACTIVE")) {
					break;
				}

				Thread.sleep(1000);
			}

			// OpenstackComputeMachine machine = new OpenstackComputeMachine(instanceInfo.getAccessIPv4(), this,
			// server.getId());

			// Even if the machine is in 'error' state, we still want to associate it with us
			if (request.tags != null) {
				Server newServerInfo = new Server();
				Metadata metadata = new Metadata();
				for (Tag tag : request.tags) {
					Metadata.Item meta = new Metadata.Item();
					meta.setKey(tag.getKey());
					meta.setValue(tag.getValue());
					metadata.getItems().add(meta);
				}

				newServerInfo.setMetadata(metadata);
				log.info("Tagging server: " + server.getId());
				computeClient.root().servers().server(server.getId()).update(newServerInfo);
			}

			return server;
		} catch (InterruptedException e) {
			ExceptionUtils.handleInterrupted(e);
			throw new OpsException("Error building server", e);
		} catch (OpenstackException e) {
			throw new OpsException("Error building server", e);
		}
	}

	private Flavor getClosestInstanceType(OpenstackComputeClient computeClient, MachineCreationRequest request)
			throws OpenstackException {
		log.info("Listing image sizes to find best size");

		Iterable<Flavor> flavors = computeClient.root().flavors().list();
		List<Flavor> candidates = Lists.newArrayList();
		for (Flavor flavor : flavors) {
			int instanceMemoryMB = flavor.getRam();
			if (request.minimumMemoryMB > instanceMemoryMB) {
				continue;
			}

			candidates.add(flavor);
		}

		Flavor bestFlavor = findCheapest(candidates);
		if (bestFlavor == null) {
			return null;
		}

		return bestFlavor;
	}

	private Flavor findCheapest(List<Flavor> candidates) {
		Flavor bestFlavor = null;
		float bestPrice = Float.MAX_VALUE;

		for (Flavor candidate : candidates) {
			float price = computePrice(candidate);
			if (price < bestPrice) {
				bestFlavor = candidate;
				bestPrice = price;
			}
		}
		return bestFlavor;
	}

	private float computePrice(Flavor flavor) {
		// We compute a synthetic per-hour price so we can choose the smallest/cheapest instance size

		float price = 0;

		// RAM is $0.10 / hour / GB
		int ram = flavor.getRam();
		price += (0.10 / 1024.0) * ram;

		// Disk is $0.10 / hour / TB
		int disk = flavor.getDisk();
		price += (0.10 / 1024.0) * disk;

		// CPUs are $0.10 / hour / vCPU
		int cpus = flavor.getVcpus();
		price += 0.10 * cpus;

		return price;
	}

	// public void ensureCreatedSecurityGroups(List<String> securityGroups) throws OpsException {
	// for (String securityGroup : securityGroups) {
	// ensureCreatedSecurityGroup(securityGroup);
	// }
	// }
	//
	// public void ensureCreatedSecurityGroup(String securityGroup) throws OpsException {
	// AmazonEC2Client ec2Api = buildEc2Client();
	//
	// CreateSecurityGroupRequest createSecurityGroupRequest = new CreateSecurityGroupRequest();
	// createSecurityGroupRequest.setGroupName(securityGroup);
	// createSecurityGroupRequest.setDescription(securityGroup);
	// try {
	// ec2Api.createSecurityGroup(createSecurityGroupRequest);
	// } catch (AmazonClientException e) {
	// String message = getEc2ErrorMessage(e);
	//
	// if (message != null && message.endsWith("already exists")) {
	// log.info("Security group already exists: " + securityGroup);
	// return;
	// }
	//
	// throw new OpsException("Error creating security group", e);
	// }
	// }
	//
	// public void ensurePortOpen(String securityGroup, String protocol, int destPort) throws OpsException {
	// AmazonEC2Client ec2Api = buildEc2Client();
	//
	// List<IpPermission> ipPermissions = Lists.newArrayList();
	//
	// IpPermission ipPermission = new IpPermission();
	// ipPermission.setFromPort(destPort);
	// ipPermission.setToPort(destPort);
	// ipPermission.setIpProtocol(protocol);
	// List<String> ipRanges = Lists.newArrayList();
	// ipRanges.add("0.0.0.0/0");
	// ipPermission.setIpRanges(ipRanges);
	//
	// ipPermissions.add(ipPermission);
	// AuthorizeSecurityGroupIngressRequest request = new AuthorizeSecurityGroupIngressRequest(securityGroup,
	// ipPermissions);
	// try {
	// ec2Api.authorizeSecurityGroupIngress(request);
	// } catch (AmazonClientException e) {
	// String message = getEc2ErrorMessage(e);
	//
	// if (message != null && message.endsWith("rule already exists in group")) {
	// log.info("Firewall rule already exists: " + securityGroup);
	// return;
	// }
	//
	// throw new OpsException("Error creating firewall rule", e);
	// }
	// }
	//
	// private String getEc2ErrorMessage(AmazonClientException e) {
	// if (e instanceof AmazonServiceException) {
	// AmazonServiceException amazonServiceException = (AmazonServiceException) e;
	// String message = amazonServiceException.getMessage();
	// if (message != null) {
	// message = message.trim();
	// return message;
	// }
	// }
	// return null;
	// }
	//
	// private String getOpenstackInstanceId(Instance instanceInfo) {
	// String ec2InstanceId = instanceInfo.getInstanceId();
	// if (!ec2InstanceId.startsWith("i-")) {
	// throw new IllegalArgumentException("Invalid instance id: " + ec2InstanceId);
	// }
	// ec2InstanceId = ec2InstanceId.substring(2);
	// Long osInstanceId = Long.parseLong(ec2InstanceId, 16);
	// return osInstanceId.toString();
	// }
	//
	// private com.amazonaws.services.ec2.model.Image mapToEc2Image(OpenstackComputeClient osApi, AmazonEC2Client
	// ec2Api, Image osImage) {
	// DescribeImagesResult ec2Images = ec2Api.describeImages();
	//
	// // String imageId = String.format("ami-%08X", Long.parseLong(osImage.getId()));
	// // for (com.amazonaws.services.ec2.model.Image ec2Image : ec2Images.getImages()) {
	// // if (imageId.equalsIgnoreCase(ec2Image.getImageId())) {
	// // return ec2Image;
	// // }
	// // }
	//
	// String imageName = "None (" + osImage.getName() + ")";
	// for (com.amazonaws.services.ec2.model.Image ec2Image : ec2Images.getImages()) {
	// if (imageName.equalsIgnoreCase(ec2Image.getImageLocation())) {
	// return ec2Image;
	// }
	// }
	//
	// return null;
	// }
	//
	// private KeyPair extractKeyPair(CreateKeyPairResult createKeyPairResult) throws OpsException {
	// String pemKeyMaterial = createKeyPairResult.getKeyPair().getKeyMaterial();
	// PEMReader r = new PEMReader(new StringReader(pemKeyMaterial));
	// try {
	// return (KeyPair) r.readObject();
	// } catch (IOException ex) {
	// throw new OpsException("Error decoding generated SSH key", ex);
	// } finally {
	// IoUtils.safeClose(r);
	// }
	// }
	//
	// private static Instance getInstanceInfo(AmazonEC2Client client, String instanceId) throws AmazonServiceException,
	// AmazonClientException {
	// DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
	// describeInstancesRequest.setInstanceIds(Collections.singleton(instanceId));
	// DescribeInstancesResult describeInstancesResult = client.describeInstances(describeInstancesRequest);
	// Instance instanceInfo = describeInstancesResult.getReservations().get(0).getInstances().get(0);
	// return instanceInfo;
	// }

	private static Server getInstanceInfo(OpenstackComputeClient computeClient, String serverId)
			throws OpenstackException {
		Server serverDetails = computeClient.root().servers().server(serverId).show();
		return serverDetails;
	}

	public void terminateInstance(OpenstackComputeMachine machine) throws OpsException {
		terminateInstance(machine.getCloud(), machine.getOpenstackServerId());
	}

	public AsyncServerOperation terminateInstance(OpenstackCloud cloud, String instanceId) throws OpsException {
		try {
			OpenstackComputeClient computeClient = getComputeClient(cloud);

			log.info("Terminating server: " + instanceId);
			AsyncServerOperation deleteOperation = computeClient.deleteServer(instanceId);
			return deleteOperation;
		} catch (OpenstackNotFoundException e) {
			log.info("Could not find instance to be terminated, assuming already terminated: " + instanceId);
			return null;
		} catch (OpenstackException e) {
			throw new OpsException("Error terminating server", e);
		}
	}

	public ImageStore getImageStore(OpenstackCloud cloud) throws OpsException {
		OpenstackCloudHelpers helpers = new OpenstackCloudHelpers();
		OpenstackImageClient openstackImageClient = helpers.buildOpenstackImageClient(cloud);

		return new GlanceImageStore(openstackImageClient);
	}

	public Server ensureHasPublicIp(final OpenstackCloud cloud, Server server) throws OpsException {
		final OpenstackCloudHelpers helpers = new OpenstackCloudHelpers();

		List<Ip> publicIps = helpers.findPublicIps(cloud, server);

		if (!publicIps.isEmpty()) {
			return server;
		}

		final OpenstackComputeClient compute = getComputeClient(cloud);

		log.info("Creating floating IP");
		FloatingIp floatingIp = compute.root().floatingIps().create();

		// TODO: Don't abandon the IP e.g. if the attach fails
		log.info("Attching floating IP " + floatingIp.getIp() + " to " + server.getId());
		compute.root().servers().server(server.getId()).addFloatingIp(floatingIp.getIp());

		final String serverId = server.getId();

		try {
			server = TimeoutPoll.poll(TimeSpan.FIVE_MINUTES, TimeSpan.TEN_SECONDS, new PollFunction<Server>() {
				@Override
				public Server call() throws Exception {
					log.info("Waiting for floating IP attach; polling server: " + serverId);
					Server server = compute.root().servers().server(serverId).show();

					List<Ip> publicIps = helpers.findPublicIps(cloud, server);
					if (publicIps.isEmpty()) {
						return null;
					}
					return server;
				}
			});
		} catch (TimeoutException e) {
			throw new OpsException("Timeout while waiting for attached public IP to show up", e);
		} catch (ExecutionException e) {
			throw new OpsException("Error while waiting for attached public IP to show up", e);
		}

		return server;
	}

	public AsyncServerOperation ensurePoweredOn(OpenstackCloud cloud, Server server) throws OpsException {
		String status = server.getStatus();
		if (Objects.equal(status, "SHUTOFF")) {
			try {
				OpenstackComputeClient computeClient = getComputeClient(cloud);

				String serverId = server.getId();
				log.info("Starting SHUTOFF server: " + serverId);

				AsyncServerOperation powerOnOperation = computeClient.powerServerOn(serverId);
				return powerOnOperation;
			} catch (OpenstackException e) {
				throw new OpsException("Error powering server on", e);
			}
		}

		return null;
	}

	// @Override
	// public Machine refreshMachine(Machine machine) throws OpsException {
	// throw new UnsupportedOperationException();
	// }
	//
	// // public String getState(String instanceId) throws OpsException {
	// // OpenstackComputeClient osApi = buildOpenstackClient();
	// // Server server;
	// // try {
	// // server = osApi.getServerDetails(instanceId);
	// // } catch (OpenstackException e) {
	// // throw new OpsException("Error getting instance info", e);
	// // }
	// // if (server == null)
	// // throw new OpsException("Instance not found: " + instanceId);
	// // return server.getStatus();
	// // }
	//
	// // public DescribeInstancesResult describeInstances() throws OpsException {
	// // DescribeInstancesRequest request = new DescribeInstancesRequest();
	// // try {
	// // return buildOpenstackClient().describeInstances(request);
	// // } catch (AmazonClientException e) {
	// // throw new OpsException("Error terminating server", e);
	// // }
	// // }
	//

	public static AsyncServerOperation wrapServerCreate(OpenstackComputeClient client, Server server)
			throws OpenstackException {
		return new AsyncServerOperation(client, server, server.getId(), Lists.newArrayList("BUILD"),
				Lists.newArrayList("ACTIVE"));
	}

}
