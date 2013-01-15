package org.platformlayer.ops.cas;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.platformlayer.cas.CasLocation;
import org.platformlayer.cas.CasPickClosestStore;
import org.platformlayer.cas.CasStore;
import org.platformlayer.cas.CasStoreInfo;
import org.platformlayer.cas.CasStoreMap;
import org.platformlayer.cas.CasStoreObject;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.cas.filesystem.FilesystemCasStore;
import org.platformlayer.ops.cas.jenkins.JenkinsCasStore;
import org.platformlayer.ops.cas.jenkins.JenkinsClient;
import org.platformlayer.ops.helpers.ProviderHelper;
import org.platformlayer.ops.helpers.ProviderHelper.ProviderOf;
import org.platformlayer.ops.helpers.SshKeys;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CasStoreHelper {
	static final Logger log = LoggerFactory.getLogger(CasStoreHelper.class);

	// List<CasStore> casStores;

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	SshKeys sshKeys;

	@Inject
	ProviderHelper providers;

	private static JenkinsCasStore buildJenkins(String baseUrl) {
		HttpClient httpClient = new DefaultHttpClient();
		JenkinsClient jenkinsClient;
		try {
			jenkinsClient = new JenkinsClient(httpClient, new URI(baseUrl));
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Error parsing URI", e);
		}
		return new JenkinsCasStore(new CasStoreInfo(false), jenkinsClient);
	}

	public CasStoreMap getCasStoreMap(OpsTarget target) throws OpsException {
		// TODO: Reintroduce (some) caching?
		// if (this.casStores == null) {
		CasStoreMap casStores = new CasStoreMap();

		FilesystemCasStore filesystemCasStore = new FilesystemCasStore(new CasStoreInfo(false),
				new OpsCasTarget(target));
		casStores.addPrimary(filesystemCasStore);

		// TODO: Don't hard-code
		casStores.addSecondary(buildJenkins("http://192.168.131.14:8080/"));
		// casStores.add(buildJenkins("http://192.168.192.36:8080/"));

		for (ProviderOf<CasStoreProvider> casStoreProvider : providers.listItemsProviding(CasStoreProvider.class)) {
			CasStore casStore = casStoreProvider.get().getCasStore();
			casStores.addSecondary(casStore);

			if (casStore.getOptions().isStaging()) {
				// Use this as a staging store i.e. we can upload files to here instead of to the VM
				casStores.addStagingStore(casStore);
			}
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
