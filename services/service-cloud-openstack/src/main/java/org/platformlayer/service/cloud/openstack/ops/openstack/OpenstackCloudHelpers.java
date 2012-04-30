package org.platformlayer.service.cloud.openstack.ops.openstack;

import java.security.PublicKey;
import java.util.List;
import java.util.UUID;

import org.openstack.client.OpenstackCredentials;
import org.openstack.client.OpenstackException;
import org.openstack.client.common.OpenstackComputeClient;
import org.openstack.client.common.OpenstackImageClient;
import org.openstack.client.common.OpenstackSession;
import org.openstack.model.compute.Addresses;
import org.openstack.model.compute.Addresses.Network;
import org.openstack.model.compute.Addresses.Network.Ip;
import org.openstack.model.compute.KeyPair;
import org.openstack.model.compute.SecurityGroup;
import org.openstack.model.compute.SecurityGroupList;
import org.openstack.model.compute.Server;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.SshKeys;
import org.platformlayer.service.cloud.openstack.model.OpenstackCloud;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class OpenstackCloudHelpers {
	public OpenstackComputeClient buildOpenstackComputeClient(OpenstackCloud cloud) throws OpsException {
		try {
			OpenstackSession session = buildOpenstackSession(cloud);

			return session.getComputeClient();
		} catch (OpenstackException e) {
			throw new OpsException("Error connecting to OpenStack compute API", e);
		}
	}

	public OpenstackImageClient buildOpenstackImageClient(OpenstackCloud cloud) throws OpsException {
		try {
			OpenstackSession session = buildOpenstackSession(cloud);

			return session.getImageClient();
		} catch (OpenstackException e) {
			throw new OpsException("Error connecting to OpenStack image API", e);
		}
	}

	private OpenstackSession buildOpenstackSession(OpenstackCloud cloud) throws OpsException {
		String authUrl = cloud.endpoint;

		String username = cloud.username;
		String secret = cloud.password.plaintext();
		String tenant = cloud.tenant;

		OpenstackCredentials credentials = new OpenstackCredentials(authUrl, username, secret, tenant);
		OpenstackSession session = OpenstackSession.create();
		session.authenticate(credentials, true);

		session.enable(OpenstackSession.Feature.VERBOSE);

		return session;
	}

	public List<Ip> findPublicIps(OpenstackCloud cloud, Server server) {
		List<Ip> ips = Lists.newArrayList();

		// {
		// String ip = server.getAccessIpV4();
		// if (!Strings.isNullOrEmpty(ip)) {
		// tags.add(new Tag(Tag.NETWORK_ADDRESS, ip));
		// }
		// }

		Addresses addresses = server.getAddresses();
		for (Network network : addresses.getNetworks()) {
			if ("public".equals(network.getId())) {
				for (Ip ip : network.getIps()) {
					if (Strings.isNullOrEmpty(ip.getAddr())) {
						continue;
					}

					ips.add(ip);
				}
			}
		}

		CloudBehaviours behaviours = new CloudBehaviours(cloud);
		if (behaviours.publicIpsReportedAsPrivate()) {
			for (Network network : addresses.getNetworks()) {
				if ("private".equals(network.getId())) {
					for (Ip ip : network.getIps()) {
						if (Strings.isNullOrEmpty(ip.getAddr())) {
							continue;
						}

						if (behaviours.isPublic(ip)) {
							ips.add(ip);
						}
					}
				}
			}
		}

		return ips;
	}

	public SecurityGroup getMachineSecurityGroup(OpenstackComputeClient openstackComputeClient, Server server)
			throws OpsException {
		// SecurityGroupList securityGroups;
		// try {
		// securityGroups = openstackComputeClient.root().servers().server(server.getId()).listSecurityGroups();
		// } catch (OpenstackException e) {
		// throw new OpsException("Error getting security groups for server", e);
		// }

		SecurityGroupList securityGroups;
		try {
			securityGroups = openstackComputeClient.root().securityGroups().list();
		} catch (OpenstackException e) {
			throw new OpsException("Error getting security groups for server", e);
		}

		SecurityGroup securityGroup = null;
		if (securityGroups != null && securityGroups.getList() != null) {
			for (SecurityGroup candidate : securityGroups.getList()) {
				if (candidate.getName() == null) {
					continue;
				}

				if (candidate.getName().equals(OpenstackCloudContext.SECURITY_GROUP_PREFIX + server.getName())) {
					securityGroup = candidate;
					break;
				}

				// if (candidate.getName().startsWith(OpenstackCloudContext.SECURITY_GROUP_PREFIX)) {
				// securityGroup = candidate;
				// break;
				// }
			}
		}

		if (securityGroup == null) {
			throw new OpsException("Could not find platform layer security group for server: " + server);
		}

		return securityGroup;
	}

	public KeyPair findPublicKey(OpenstackComputeClient compute, PublicKey sshPublicKey) throws OpsException {
		String publicKey = SshKeys.serialize(sshPublicKey);
		for (KeyPair keyPair : compute.root().keyPairs().list()) {
			if (publicKey.equals(keyPair.getPublicKey())) {
				return keyPair;
			}
		}
		return null;
	}

	public KeyPair ensurePublicKeyUploaded(OpenstackComputeClient compute, String name, PublicKey sshPublicKey)
			throws OpsException {
		KeyPair keyPair = findPublicKey(compute, sshPublicKey);

		if (keyPair == null) {
			if (name == null) {
				name = UUID.randomUUID().toString();
			}

			String publicKey = SshKeys.serialize(sshPublicKey);
			KeyPair create = new KeyPair();
			create.setName(name);
			create.setPublicKey(publicKey);
			compute.root().keyPairs().create(create);
		}

		keyPair = findPublicKey(compute, sshPublicKey);
		if (keyPair == null) {
			throw new OpsException("Created key pair was not found");
		}

		return keyPair;
	}

}
