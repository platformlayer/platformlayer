package org.platformlayer.ops.filesystem;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;

import org.platformlayer.TimeSpan;
import org.platformlayer.crypto.Md5Hash;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.CommandEnvironment;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.CurlRequest;
import org.platformlayer.ops.proxy.HttpProxyHelper;
import org.platformlayer.ops.proxy.HttpProxyHelper.Usage;

public class DownloadFile extends ManagedFile {
	public URI url;
	public Md5Hash hash;

	@Inject
	HttpProxyHelper httpProxies;

	@Override
	protected void uploadFile(OpsTarget target, File remoteFilePath) throws IOException, OpsException {
		target.mkdir(remoteFilePath.getParentFile());

		CurlRequest curlRequest = new CurlRequest(url);
		curlRequest.bareRequest = true;

		CommandEnvironment commandEnvironment = httpProxies.getHttpProxyEnvironment(target, Usage.General, url);

		Command curlCommand = curlRequest.toCommand();
		curlCommand.addLiteral(">");
		curlCommand.addFile(remoteFilePath);
		curlCommand.setEnvironment(commandEnvironment);
		curlCommand.setTimeout(TimeSpan.FIVE_MINUTES);

		target.executeCommand(curlCommand);
	}

	@Override
	protected Md5Hash getSourceMd5(OpsTarget target) throws OpsException {
		if (hash == null) {
			// TODO: Figure out the hash (efficiently!)
			// It is much more secure _not_ to do this
			throw new UnsupportedOperationException();
		}

		return hash;
	}

	public void setUrl(String url) {
		try {
			this.url = new URI(url);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Error parsing URI", e);
		}
	}
}
