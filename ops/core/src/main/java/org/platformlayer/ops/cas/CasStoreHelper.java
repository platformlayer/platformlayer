package org.platformlayer.ops.cas;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.openstack.client.OpenstackCredentials;
import org.platformlayer.cas.CasLocation;
import org.platformlayer.cas.CasPickClosestStore;
import org.platformlayer.cas.CasStore;
import org.platformlayer.cas.CasStoreMap;
import org.platformlayer.cas.CasStoreObject;
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
		HttpClient httpClient = new DefaultHttpClient();
		JenkinsClient jenkinsClient;
		try {
			jenkinsClient = new JenkinsClient(httpClient, new URI(baseUrl));
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Error parsing URI", e);
		}
		return new JenkinsCasStore(jenkinsClient);
	}

	public CasStoreMap getCasStoreMap(OpsTarget target) throws OpsException {
		// TODO: Reintroduce (some) caching?
		// if (this.casStores == null) {
		CasStoreMap casStores = new CasStoreMap();

		FilesystemCasStore filesystemCasStore = new FilesystemCasStore(new OpsCasTarget(target));
		casStores.addPrimary(filesystemCasStore);

		// TODO: Don't hard-code
		casStores.addSecondary(buildJenkins("http://192.168.131.14:8080/"));
		// casStores.add(buildJenkins("http://192.168.192.36:8080/"));

		for (OpenstackCredentials credentials : openstackClouds.findOpenstackClouds()) {
			casStores.addSecondary(buildOpenstack(credentials));
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

			FilesystemCasStore store = new FilesystemCasStore(new OpsCasTarget(machineTarget));
			casStores.addSecondary(store);

			// Use this as a staging store i.e. we can upload files to here instead of to the VM
			casStores.addStagingStore(store);
		}

		// this.casStores = casStores;
		// }
		return casStores;
	}

	public void copyObject(CasStoreMap casStoreMap, CasStoreObject src, OpsCasTarget opsCasTarget, File remoteFilePath,
			boolean useStagingStore) throws OpsException {
		log.info("Copying object from " + src + "  to " + opsCasTarget);
		try {
			CasLocation targetLocation = opsCasTarget.getLocation();

			CasStore stagingStore = null;
			if (useStagingStore) {
				// Find the nearest staging store
				CasPickClosestStore pickClosest = new CasPickClosestStore(targetLocation);
				stagingStore = pickClosest.choose(casStoreMap.getStagingStores());
			}

			if (stagingStore != null) {
				if (stagingStore.equals(src.getStore())) {
					log.info("Already on closest staging server");
					stagingStore = null;
				}
			}

			src.copyTo(opsCasTarget, remoteFilePath, stagingStore);
		} catch (Exception e) {
			throw new OpsException("Error copying file to remote CAS", e);
		}
	}
}
