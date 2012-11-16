package org.platformlayer.ops.cas.jenkins;

import java.net.URI;
import java.util.List;

import org.apache.log4j.Logger;
import org.platformlayer.cas.CasLocation;
import org.platformlayer.cas.CasStore;
import org.platformlayer.cas.CasStoreObject;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.cas.OpsCasLocation;
import org.platformlayer.ops.cas.jenkins.JenkinsClient.BuildId;
import org.platformlayer.ops.cas.jenkins.JenkinsClient.BuildInfo;
import org.platformlayer.ops.cas.jenkins.JenkinsClient.BuildInfo.ArtifactInfo;
import org.platformlayer.ops.cas.jenkins.JenkinsClient.FingerprintInfo;
import org.platformlayer.ops.networks.NetworkPoint;

import com.fathomdb.hash.Md5Hash;

public class JenkinsCasStore implements CasStore {
	static final Logger log = Logger.getLogger(JenkinsCasStore.class);

	final JenkinsClient client;

	public JenkinsCasStore(JenkinsClient client) {
		this.client = client;
	}

	@Override
	public CasStoreObject findArtifact(Md5Hash hash) throws OpsException {
		try {
			FingerprintInfo fingerprint = client.findByFingerprint(hash.toHex());
			if (fingerprint == null) {
				return null;
			}

			BuildId build = fingerprint.getOriginalBuild();
			if (build == null) {
				build = fingerprint.getFirstUsage();
				if (build == null) {
					log.warn("Cannot find build for fingerprint: " + hash.toHex());
					return null;
				}
			}

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
			return new JenkinsCasObject(this, hash, url);
		} catch (JenkinsException e) {
			throw new OpsException("Error communicating with Jenkins", e);
		}
	}

	@Override
	public Md5Hash findTag(String tag) throws OpsException {
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

		BuildInfo buildInfo;

		try {
			buildInfo = client.findPromotedBuild(jobKey, promotionKey, treeFilter);
			if (buildInfo == null) {
				return null;
			}
		} catch (JenkinsException e) {
			throw new OpsException("Error communicating with Jenkins", e);
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

	public CasLocation getLocation() throws OpsException {
		return new OpsCasLocation(NetworkPoint.forPublicHostname(client.getBaseUrl().getHost()));
	}

	@Override
	public int estimateDistance(CasLocation target) throws OpsException {
		return getLocation().estimateDistance(target);
	}

}
