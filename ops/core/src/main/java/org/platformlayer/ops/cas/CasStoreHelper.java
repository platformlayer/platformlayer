package org.platformlayer.ops.cas;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.log4j.Logger;
import org.openstack.client.OpenstackCredentials;
import org.openstack.crypto.Md5Hash;
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

import com.google.common.collect.Lists;

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

	public CasObject findArtifact(OpsTarget target, Md5Hash hash) throws OpsException {
		FilesystemCasStore filesystemCasStore = new FilesystemCasStore(target);
		CasObject found = tryFind(filesystemCasStore, hash);
		if (found != null) {
			// We're not going to do better
			return found;
		}

		List<CasObject> matches = Lists.newArrayList();

		for (CasStore casStore : getCasStores()) {
			found = tryFind(casStore, hash);
			if (found != null) {
				matches.add(found);
			}
		}

		CasPickClosest chooser = new CasPickClosest(target);

		if (log.isDebugEnabled()) {
			log.debug("Found " + matches.size() + " CAS copies");
			for (CasObject match : matches) {
				log.debug("\t" + match + " => " + chooser.score(match));
			}
		}

		return chooser.choose(matches);
	}

	private List<CasStore> getCasStores() throws OpsException {
		// TODO: Reintroduce (some) caching?
		// if (this.casStores == null) {
		List<CasStore> casStores = Lists.newArrayList();
		// TODO: Don't hard-code
		casStores.add(buildJenkins("http://192.168.128.1:8080/"));
		casStores.add(buildJenkins("http://192.168.192.36:8080/"));

		for (OpenstackCredentials credentials : openstackClouds.findOpenstackClouds()) {
			casStores.add(buildOpenstack(credentials));
		}

		// TODO: This is evil
		for (DirectHost host : platformLayer.listItems(DirectHost.class)) {
			NetworkPoint targetAddress = NetworkPoint.forPublicHostname(host.getHost());
			Machine machine = new OpaqueMachine(targetAddress);
			OpsTarget target = machine.getTarget(sshKeys.findOtherServiceKey(new ServiceType("machines-direct")));

			casStores.add(new FilesystemCasStore(target));
		}

		// this.casStores = casStores;
		// }
		return casStores;
	}

	private CasObject tryFind(CasStore casStore, Md5Hash hash) {
		try {
			CasObject uri = casStore.findArtifact(hash);
			if (uri != null) {
				return uri;
			}
		} catch (Exception e) {
			log.warn("Error while resolving artifact in " + casStore, e);
		}
		return null;
	}

}
