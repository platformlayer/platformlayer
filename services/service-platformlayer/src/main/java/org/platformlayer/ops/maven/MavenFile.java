package org.platformlayer.ops.maven;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.openstack.client.OpenstackCredentials;
import org.openstack.crypto.Md5Hash;
import org.openstack.filesystem.HashAttributes;
import org.openstack.filesystem.OpenstackFileSystem;
import org.openstack.filesystem.OpenstackPath;
import org.platformlayer.TimeSpan;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.filesystem.ManagedFile;
import org.platformlayer.ops.openstack.DirectOpenstackDownload;

public class MavenFile extends ManagedFile {
	public Path basePath;
	public MavenReference mavenReference;
	public File expandPath;
	public File repositoryPath;

	private Path resolvedArtifact;

	Path getResolvedArtifact() throws OpsException {
		try {
			if (resolvedArtifact == null) {
				MavenResolver resolver = new MavenResolver(basePath);
				resolvedArtifact = resolver.resolve(mavenReference);
			}
			if (resolvedArtifact == null) {
				throw new OpsException("Unable to resolve: " + mavenReference);
			}
			return resolvedArtifact;
		} catch (IOException e) {
			throw new OpsException("Error resolving artifact", e);
		}
	}

	@Override
	protected void uploadFile(OpsTarget target, File remoteFilePath) throws IOException, OpsException {
		Path resolved = null;
		resolved = getResolvedArtifact();

		uploadFile(target, remoteFilePath, (OpenstackPath) resolved);
	}

	private void uploadFile(OpsTarget target, File remoteFilePath, OpenstackPath resolved) throws OpsException {
		target.mkdir(remoteFilePath.getParentFile());

		OpenstackFileSystem fileSystem = resolved.getFileSystem();

		OpenstackCredentials credentials = fileSystem.getOpenstackCredentials();

		String containerName = resolved.getContainerName();
		String objectPath = resolved.getObjectPath();

		DirectOpenstackDownload download = new DirectOpenstackDownload();
		download.download(target, remoteFilePath, credentials, containerName, objectPath);
	}

	@Override
	protected Md5Hash getSourceMd5(OpsTarget target) throws OpsException {
		Path resolved = null;
		byte[] md5 = null;
		try {
			resolved = getResolvedArtifact();

			HashAttributes hashAttributes = Files.readAttributes(resolved, HashAttributes.class);
			if (hashAttributes != null) {
				md5 = hashAttributes.getHash(HashAttributes.Algorithm.MD5.toString());
			}
		} catch (IOException e) {
			throw new OpsException("Error resolving artifact", e);
		}

		if (md5 == null) {
			throw new OpsException("Cannot get MD5 for: " + resolved);
		}

		return new Md5Hash(md5);
	}

	@Override
	protected void doUpdateAction(OpsTarget target) throws OpsException {
		if (expandPath == null) {
			return;
		}

		if (OpsContext.isConfigure()) {
			File zipPath = getFilePath();

			target.mkdir(expandPath);

			// -u = update, for (something close to) idempotency
			// -o = overwrite (no prompt)
			Command unzipCommand = Command.build("unzip -u -o {0} -d {1}", zipPath, expandPath);
			target.executeCommand(unzipCommand.setTimeout(TimeSpan.FIVE_MINUTES));
		}
	}

	@Override
	protected File getFilePath() throws OpsException {
		Path resolved = getResolvedArtifact();

		Path relativePath = basePath.relativize(resolved);

		File filePath = new File(repositoryPath, relativePath.toString());

		return filePath;
	}
}