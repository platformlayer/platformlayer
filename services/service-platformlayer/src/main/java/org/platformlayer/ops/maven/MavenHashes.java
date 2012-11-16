package org.platformlayer.ops.maven;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.openstack.utils.Io;

import com.fathomdb.hash.Md5Hash;

public class MavenHashes {
	public static Md5Hash getMd5(Path artifactPath) throws IOException {
		String fileName = artifactPath.getFileName().toString();

		Path md5Path = artifactPath.resolveSibling(fileName + ".md5");

		InputStream is = null;

		try {
			is = Files.newInputStream(md5Path);
			String md5 = Io.readAll(is);
			return new Md5Hash(md5);
		} finally {
			Io.safeClose(is);
		}
	}
}
