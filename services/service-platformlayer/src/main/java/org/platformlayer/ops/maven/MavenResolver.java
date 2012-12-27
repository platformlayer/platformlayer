package org.platformlayer.ops.maven;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.log4j.Logger;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.platformlayer.maven.MavenXml;

import com.fathomdb.io.IoUtils;
import com.google.common.base.Objects;

public class MavenResolver {
	static final Logger log = Logger.getLogger(MavenResolver.class);

	final Path basePath;

	public MavenResolver(Path basePath) {
		this.basePath = basePath;
	}

	public Path resolve(MavenReference reference) throws IOException {
		Path artifactPath = toPath(reference.groupId, reference.artifactId);

		if (reference.versionId == null) {
			reference.versionId = resolveVersion(artifactPath, reference);
		}

		if (reference.classifier == null) {
			reference.classifier = "jar";
		}

		Path versionedPath = artifactPath.resolve(reference.versionId);
		Path artifactMavenMetadataPath = versionedPath.resolve("maven-metadata.xml");

		log.info("Reading file: " + artifactMavenMetadataPath);
		String mavenMetadataXml = IoUtils.readAll(Files.newInputStream(artifactMavenMetadataPath));

		Metadata mavenMetadata = MavenXml.readMetadata(mavenMetadataXml);

		return pickSnapshot(versionedPath, reference, mavenMetadata);
	}

	String resolveVersion(Path artifactPath, MavenReference reference) throws IOException {
		Path artifactMavenMetadataPath = artifactPath.resolve("maven-metadata.xml");

		log.info("Reading file: " + artifactMavenMetadataPath);
		String mavenMetadataXml = IoUtils.readAll(Files.newInputStream(artifactMavenMetadataPath));

		Metadata mavenMetadata = MavenXml.readMetadata(mavenMetadataXml);

		String version = pickVersion(mavenMetadata.getVersioning());
		if (version == null) {
			throw new IllegalStateException("Could not find version");
		}

		return version;
	}

	private Path toPath(String groupId, String artifactId) {
		String groupPathString = groupId.replace('.', '/');
		Path groupPath = basePath.resolve(groupPathString);

		Path artifactPath = groupPath.resolve(artifactId);
		return artifactPath;
	}

	private Path pickSnapshot(Path versionedPath, MavenReference reference, Metadata mavenMetadata) {
		// Snapshot snapshot = mavenMetadata.getVersioning().getSnapshot();
		// String timestamp = snapshot.getTimestamp();
		// String buildNumber = snapshot.getBuildNumber();

		SnapshotVersion found = null;
		for (SnapshotVersion snapshotVersion : mavenMetadata.getVersioning().getSnapshotVersions()) {
			String classifier = snapshotVersion.getClassifier();
			String extension = snapshotVersion.getExtension();
			if (classifier == null) {
				classifier = extension;
			}

			if (Objects.equal(classifier, reference.classifier)) {
				if (found != null) {
					throw new IllegalStateException("Multiple matches found");
				}
				found = snapshotVersion;
			}
		}

		if (found == null) {
			throw new IllegalStateException("Cannot find artifact: " + reference);
		}

		String name = reference.artifactId + "-" + found.getVersion();
		if (found.getClassifier() != null) {
			name += "-" + found.getClassifier();
		}

		name += "." + found.getExtension();

		Path resolvedPath = versionedPath.resolve(name);
		return resolvedPath;
	}

	private String pickVersion(Versioning versioning) {
		String best = null;

		for (String version : versioning.getVersions()) {
			if (best == null) {
				best = version;
			} else {
				throw new UnsupportedOperationException();
			}
		}
		return best;
	}
}
