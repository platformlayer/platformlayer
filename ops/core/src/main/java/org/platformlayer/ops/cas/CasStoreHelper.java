package org.platformlayer.ops.cas;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.log4j.Logger;
import org.openstack.client.OpenstackCredentials;
import org.platformlayer.crypto.Md5Hash;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.cas.filesystem.FilesystemCasStore;
import org.platformlayer.ops.cas.jenkins.JenkinsCasStore;
import org.platformlayer.ops.cas.jenkins.JenkinsClient;
import org.platformlayer.ops.cas.openstack.OpenstackCasStore;
import org.platformlayer.ops.openstack.OpenstackCloudHelpers;

import com.google.common.collect.Lists;

public class CasStoreHelper {
	static final Logger log = Logger.getLogger(CasStoreHelper.class);

	// List<CasStore> casStores;

	@Inject
	OpenstackCloudHelpers openstackClouds;

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
			return found;
		}

		for (CasStore casStore : getCasStores()) {
			found = tryFind(casStore, hash);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	private List<CasStore> getCasStores() throws OpsException {
		// if (this.casStores == null) {
		List<CasStore> casStores = Lists.newArrayList();
		// TODO: Don't hard-code
		casStores.add(buildJenkins("http://192.168.128.1:8080/"));
		casStores.add(buildJenkins("http://192.168.192.36:8080/"));

		for (OpenstackCredentials credentials : openstackClouds.findOpenstackClouds()) {
			casStores.add(buildOpenstack(credentials));
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
