package org.platformlayer.ops.cas.jenkins;

import java.net.URI;
import java.util.List;

import org.apache.log4j.Logger;
import org.openstack.crypto.ByteString;
import org.openstack.crypto.Md5Hash;
import org.platformlayer.cas.CasStore;
import org.platformlayer.cas.CasStoreObject;
import org.platformlayer.ops.cas.jenkins.JenkinsClient.BuildId;
import org.platformlayer.ops.cas.jenkins.JenkinsClient.BuildInfo;
import org.platformlayer.ops.cas.jenkins.JenkinsClient.BuildInfo.ArtifactInfo;
import org.platformlayer.ops.cas.jenkins.JenkinsClient.FingerprintInfo;

public class JenkinsCasStore implements CasStore {
	static final Logger log = Logger.getLogger(JenkinsCasStore.class);

	final JenkinsClient client;

	public JenkinsCasStore(JenkinsClient client) {
		this.client = client;
	}

	@Override
	public CasStoreObject findArtifact(ByteString hash) throws JenkinsException {
		FingerprintInfo fingerprint = client.findByFingerprint(hash.toHex());
		if (fingerprint == null) {
			return null;
		}

		BuildId build = fingerprint.getOriginalBuild();

		BuildInfo buildInfo = client.findBuildInfo(build);
		if (buildInfo == null) {
			return null;
		}

		String fingerprintFileName = fingerprint.getFileName();
		ArtifactInfo found = buildInfo.findArtifactByFileName(fingerprintFileName);
		if (found == null) {
			log.warn("Could not find artifact: " + fingerprint + " in " + buildInfo);
			return null;
		}

		URI url = found.getArtifactUrl();
		return new JenkinsCasObject(hash, url);
	}

	@Override
	public ByteString findTag(String tag) throws Exception {
		URI uri = client.getBaseUrl();

		// TODO: Match jenkins host??

		String[] tokens = tag.split(":");
		if (tokens.length != 3) {
			return null;
		}

		String jobKey = tokens[0];
		String promotionKey = tokens[1];
		String fileName = tokens[2];

		String treeFilter = "fingerprint[fileName,hash]";

		BuildInfo buildInfo = client.findPromotedBuild(jobKey, promotionKey, treeFilter);
		if (buildInfo == null) {
			return null;
		}

		FingerprintInfo found = null;

		List<FingerprintInfo> fingerprints = buildInfo.getFingerprints();
		for (FingerprintInfo fingerprint : fingerprints) {
			if (fileName.equals(fingerprint.getFileName())) {
				found = fingerprint;
			}
		}

		if (found == null) {
			log.warn("Could not find fingerprinted file with name: " + fileName + " in " + buildInfo);
			return null;
		}

		String hash = found.getHash();
		if (hash == null) {
			throw new IllegalStateException();
		}

		// We return the hash in the hope that we've already copied the artifact!
		return new Md5Hash(hash);
	}
}
