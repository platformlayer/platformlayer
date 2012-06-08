package org.platformlayer.ops.cas.jenkins;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.openstack.utils.Io;
import org.platformlayer.TimeSpan;
import org.platformlayer.crypto.Md5Hash;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.CommandEnvironment;
import org.platformlayer.ops.FileUpload;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.cas.CasObject;
import org.platformlayer.ops.helpers.CurlRequest;
import org.platformlayer.ops.machines.InetAddressUtils;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.proxy.HttpProxyHelper;
import org.platformlayer.ops.proxy.HttpProxyHelper.Usage;

public class JenkinsCasObject implements CasObject {
	private static final Logger log = Logger.getLogger(JenkinsCasObject.class);

	@Inject
	HttpProxyHelper httpProxies;

	private final URI uri;

	private final Md5Hash hash;

	public JenkinsCasObject(URI uri, Md5Hash hash) {
		this.uri = uri;
		this.hash = hash;
		Injection.injectMembers(this);
	}

	@Override
	public void copyTo(OpsTarget target, File remoteFilePath) throws OpsException {
		InetAddress host;
		try {
			host = InetAddress.getByName(uri.getHost());
		} catch (UnknownHostException e) {
			throw new OpsException("Unable to resolve host: " + uri, e);
		}

		if (InetAddressUtils.isPublic(host)) {
			CurlRequest curlRequest = new CurlRequest(uri);
			curlRequest.bareRequest = true;
			CommandEnvironment commandEnvironment = httpProxies.getHttpProxyEnvironment(target, Usage.General, uri);

			Command curlCommand = curlRequest.toCommand();
			curlCommand.addLiteral(">");
			curlCommand.addFile(remoteFilePath);
			curlCommand.setEnvironment(commandEnvironment);
			curlCommand.setTimeout(TimeSpan.FIVE_MINUTES);

			target.executeCommand(curlCommand);
		} else {
			log.warn("Address was not public: " + host + ", doing copy via ops system");

			File tempFile;
			try {
				tempFile = File.createTempFile("jenkins", "dat");
			} catch (IOException e) {
				throw new OpsException("Error creating temp file", e);
			}
			try {
				InputStream is = uri.toURL().openStream();
				try {
					Io.copyStreams(is, tempFile);
				} finally {
					Io.safeClose(is);
				}

				FileUpload.upload(target, remoteFilePath, tempFile);
			} catch (IOException e) {
				throw new OpsException("Error copying jenkins artifact", e);
			} finally {
				tempFile.delete();
			}
		}

	}

	@Override
	public NetworkPoint getLocation() throws OpsException {
		return NetworkPoint.forPublicHostname(uri.getHost());
	}

	@Override
	public Md5Hash getHash() {
		return hash;
	}

}
