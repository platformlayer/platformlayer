package org.platformlayer.ops.filesystem;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;

import org.openstack.crypto.ByteString;
import org.openstack.crypto.Md5Hash;
import org.platformlayer.TimeSpan;
import org.platformlayer.cas.CasStoreMap;
import org.platformlayer.cas.CasStoreObject;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.CommandEnvironment;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.cas.CasStoreHelper;
import org.platformlayer.ops.cas.OpsCasTarget;
import org.platformlayer.ops.helpers.CurlRequest;
import org.platformlayer.ops.proxy.HttpProxyHelper;
import org.platformlayer.ops.proxy.HttpProxyHelper.Usage;

public class DownloadFileByHash extends ManagedFile {
	public Md5Hash hash;

	public String specifier;

	public URI url;

	@Inject
	CasStoreHelper cas;

	@Inject
	HttpProxyHelper httpProxies;

	Md5Hash resolved;

	public String getHumanName() {
		StringBuilder sb = new StringBuilder();

		if (specifier != null) {
			sb.append("specifier=" + specifier);
		}

		if (hash != null) {
			sb.append("hash=" + hash.toHex());
		}

		if (url != null) {
			sb.append("url=" + url);
		}

		if (sb.length() == 0) {
			sb.append("[NO FIELDS SET??]");
		}

		return sb.toString();
	}

	public Md5Hash getResolved(OpsTarget target) throws OpsException {
		if (resolved == null) {
			if (hash == null) {
				if (specifier != null) {
					CasStoreMap casStores = cas.getCasStoreMap(target);
					resolved = (Md5Hash) casStores.resolve(specifier);
				}
			} else {
				resolved = hash;
			}
		}

		if (resolved == null) {
			throw new OpsException("Unable to resolve artifact: " + getHumanName());
		}

		return resolved;
	}

	@Override
	protected void uploadFile(OpsTarget target, File remoteFilePath) throws IOException, OpsException {
		target.mkdir(remoteFilePath.getParentFile());

		ByteString resolved = getResolved(target);

		CasStoreObject casObject;
		CasStoreMap casStoreMap = cas.getCasStoreMap(target);

		try {
			casObject = casStoreMap.findArtifact(new OpsCasTarget(target), resolved);
		} catch (Exception e) {
			throw new OpsException("Error while resolving artifact:" + getHumanName(), e);
		}

		if (url != null && casObject == null) {
			target.mkdir(remoteFilePath.getParentFile());

			CurlRequest curlRequest = new CurlRequest(url);
			curlRequest.bareRequest = true;

			CommandEnvironment commandEnvironment = httpProxies.getHttpProxyEnvironment(target, Usage.General, url);

			Command curlCommand = curlRequest.toCommand();
			curlCommand.addLiteral(">");
			curlCommand.addFile(remoteFilePath);
			curlCommand.setEnvironment(commandEnvironment);
			curlCommand.setTimeout(TimeSpan.FIVE_MINUTES);

			// TODO: Can we cache into CAS instead??
			log.info("Not found in CAS system; downloading directly: " + url);
			target.executeCommand(curlCommand);
		} else {
			if (casObject == null) {
				throw new OpsException("Unable to find artifact: " + getHumanName());
			}

			log.info("Doing a CAS copy from " + casObject + " to target");

			cas.copyObject(casStoreMap, casObject, new OpsCasTarget(target), remoteFilePath, true);
		}
	}

	@Override
	protected Md5Hash getSourceMd5(OpsTarget target) throws OpsException {
		Md5Hash resolved = getResolved(target);

		if (resolved == null) {
			throw new IllegalStateException("Hash could not be determined (file could not be resolved?)");
		}

		return resolved;
	}

	public void setUrl(String url) {
		try {
			this.url = new URI(url);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Error parsing URI", e);
		}
	}
}
