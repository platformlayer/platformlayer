package org.platformlayer.ops.filesystem;

import java.io.File;
import java.io.IOException;

import org.platformlayer.TimeSpan;
import org.platformlayer.crypto.Md5Hash;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.CurlRequest;

public class DownloadFile extends ManagedFile {
	public String url;
	public Md5Hash hash;

	@Override
	protected void uploadFile(OpsTarget target, File remoteFilePath) throws IOException, OpsException {
		target.mkdir(remoteFilePath.getParentFile());

		CurlRequest curlRequest = new CurlRequest(url);
		curlRequest.bareRequest = true;

		Command curlCommand = curlRequest.toCommand();
		curlCommand.addLiteral(">");
		curlCommand.addFile(remoteFilePath);

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
}
