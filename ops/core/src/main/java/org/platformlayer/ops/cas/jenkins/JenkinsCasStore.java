package org.platformlayer.ops.cas.jenkins;

import java.net.URI;

import org.apache.log4j.Logger;
import org.platformlayer.crypto.Md5Hash;
import org.platformlayer.ops.cas.CasObject;
import org.platformlayer.ops.cas.CasStore;
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
	public CasObject findArtifact(Md5Hash hash) throws JenkinsException {
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
		ArtifactInfo found = null;
		for (ArtifactInfo artifact : buildInfo.getArtifacts()) {
			if (artifact.getFileName().equals(fingerprintFileName)) {
				found = artifact;
				break;
			}
		}

		if (found == null) {
			log.warn("Could not find artifact: " + fingerprint + " in " + buildInfo);
			return null;
		}

		URI url = found.getArtifactUrl();
		return new JenkinsCasObject(url);
	}
}
