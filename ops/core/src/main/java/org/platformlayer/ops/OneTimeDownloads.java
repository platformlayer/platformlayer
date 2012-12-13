package org.platformlayer.ops;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import org.apache.commons.codec.binary.Hex;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Singleton
public class OneTimeDownloads {

	public static class Download {
		final String content;
		final String contentType;

		public Download(String content, String contentType) {
			super();
			this.content = content;
			this.contentType = contentType;
		}

		public String getContent() {
			return content;
		}

		public String getContentType() {
			return contentType;
		}

	};

	final Cache<String, Download> downloads = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();

	public Download get(String blobId) {
		Download download = downloads.getIfPresent(blobId);
		if (download != null) {
			downloads.invalidate(blobId);
		}
		return download;
	}

	final SecureRandom r = new SecureRandom();

	public String put(Download download) {
		String key;
		synchronized (r) {
			byte[] b = new byte[32];
			r.nextBytes(b);
			key = Hex.encodeHexString(b);
		}

		downloads.put(key, download);
		return key;
	}
}
