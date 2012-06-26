package org.platformlayer.ops.cas;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.log4j.Logger;
import org.openstack.client.OpenstackCredentials;
import org.platformlayer.cas.CasStoreList;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpaqueMachine;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.cas.filesystem.FilesystemCasStore;
import org.platformlayer.ops.cas.jenkins.JenkinsCasStore;
import org.platformlayer.ops.cas.jenkins.JenkinsClient;
import org.platformlayer.ops.cas.openstack.OpenstackCasStore;
import org.platformlayer.ops.helpers.SshKeys;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.openstack.OpenstackCloudHelpers;
import org.platformlayer.service.machines.direct.v1.DirectHost;

public class CasStoreHelper {
	static final Logger log = Logger.getLogger(CasStoreHelper.class);

	// List<CasStore> casStores;

	@Inject
	OpenstackCloudHelpers openstackClouds;

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	SshKeys sshKeys;

	private static OpenstackCasStore buildOpenstack(OpenstackCredentials credentials) {
		String containerName = "platformlayer-artifacts";
		return new OpenstackCasStore(credentials, containerName);
	}

	private static JenkinsCasStore buildJenkins(String baseUrl) {
		HttpClient httpClient = new HttpClient();
		JenkinsClient jenkinsClient;
		try {
			jenkinsClient = new JenkinsClient(httpClient, new URI(baseUrl));
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Error parsing URI", e);
		}
		return new JenkinsCasStore(jenkinsClient);
	}

	public CasStoreList getCasStores(OpsTarget target) throws OpsException {
		// TODO: Reintroduce (some) caching?
		// if (this.casStores == null) {
		CasStoreList casStores = new CasStoreList();

		FilesystemCasStore filesystemCasStore = new FilesystemCasStore(new OpsCasTarget(target));
		casStores.addPrimary(filesystemCasStore);

		// TODO: Don't hard-code
		casStores.add(buildJenkins("http://192.168.128.1:8080/"));
		// casStores.add(buildJenkins("http://192.168.192.36:8080/"));

		for (OpenstackCredentials credentials : openstackClouds.findOpenstackClouds()) {
			casStores.add(buildOpenstack(credentials));
		}

		// TODO: This is evil
		for (DirectHost host : platformLayer.listItems(DirectHost.class)) {
			// TODO: Getting the IP like this is also evil
			NetworkPoint targetAddress;
			// if (host.getIpv6() != null) {
			// IpRange ipv6Range = IpV6Range.parse(host.getIpv6());
			// targetAddress = NetworkPoint.forPublicHostname(ipv6Range.getGatewayAddress());
			// } else {
			targetAddress = NetworkPoint.forPublicHostname(host.getHost());
			// }

			Machine machine = new OpaqueMachine(targetAddress);
			OpsTarget machineTarget = machine
					.getTarget(sshKeys.findOtherServiceKey(new ServiceType("machines-direct")));

			casStores.add(new FilesystemCasStore(new OpsCasTarget(machineTarget)));
		}

		// this.casStores = casStores;
		// }
		return casStores;
	}

}
