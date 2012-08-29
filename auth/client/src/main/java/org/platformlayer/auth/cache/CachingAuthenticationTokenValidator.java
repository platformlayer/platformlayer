package org.platformlayer.auth.cache;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.platformlayer.auth.AuthenticationTokenValidator;
import org.platformlayer.crypto.CertificateUtils;
import org.platformlayer.metrics.CacheMetricsReporter;
import org.platformlayer.metrics.HasMetrics;
import org.platformlayer.metrics.MetricKey;
import org.platformlayer.metrics.MetricsSystem;
import org.platformlayer.model.AuthenticationToken;
import org.platformlayer.model.ProjectAuthorization;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class CachingAuthenticationTokenValidator implements AuthenticationTokenValidator, HasMetrics {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(CachingAuthenticationTokenValidator.class);

	static final int CACHE_MAX_SIZE = 1000;
	static final int CACHE_VALIDITY_MINUTES = 5;

	final AuthenticationTokenValidator inner;

	public CachingAuthenticationTokenValidator(AuthenticationTokenValidator inner) {
		this.inner = inner;
	}

	static class CertificateChainData {
		final byte[] data;

		public CertificateChainData(X509Certificate[] chain) {
			this.data = CertificateUtils.serialize(chain);
		}

		public X509Certificate[] buildCertificates() {
			return CertificateUtils.deserialize(data);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(data);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			CertificateChainData other = (CertificateChainData) obj;
			if (!Arrays.equals(data, other.data)) {
				return false;
			}
			return true;
		}

	};

	static class CacheKey {
		final String projectKey;
		final AuthenticationToken token;
		final CertificateChainData chain;

		CacheKey(String projectKey, AuthenticationToken token) {
			this.projectKey = projectKey;
			this.token = token;
			this.chain = null;
		}

		CacheKey(String projectKey, X509Certificate[] chain) {
			this.projectKey = projectKey;
			this.token = null;
			this.chain = new CertificateChainData(chain);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((chain == null) ? 0 : chain.hashCode());
			result = prime * result + ((projectKey == null) ? 0 : projectKey.hashCode());
			result = prime * result + ((token == null) ? 0 : token.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			CacheKey other = (CacheKey) obj;
			if (chain == null) {
				if (other.chain != null) {
					return false;
				}
			} else if (!chain.equals(other.chain)) {
				return false;
			}
			if (projectKey == null) {
				if (other.projectKey != null) {
					return false;
				}
			} else if (!projectKey.equals(other.projectKey)) {
				return false;
			}
			if (token == null) {
				if (other.token != null) {
					return false;
				}
			} else if (!token.equals(other.token)) {
				return false;
			}
			return true;
		}

	};

	static class CachedResult {
		final ProjectAuthorization authorization;

		public CachedResult(ProjectAuthorization authorization) {
			this.authorization = authorization;
		}

	};

	final LoadingCache<CacheKey, CachedResult> cache = CacheBuilder.newBuilder()
	// limit size
			.maximumSize(CACHE_MAX_SIZE)
			// max validity
			.expireAfterWrite(CACHE_VALIDITY_MINUTES, TimeUnit.MINUTES)

			// We want stats
			.recordStats()

			// Load on demand
			.build(new CacheLoader<CacheKey, CachedResult>() {
				@Override
				public CachedResult load(CacheKey key) throws Exception {
					return loadForCache(key);
				}
			});

	@Override
	public ProjectAuthorization validateToken(AuthenticationToken token, String projectKey) {
		CacheKey key = new CacheKey(projectKey, token);

		return authenticate(key);
	}

	private ProjectAuthorization authenticate(CacheKey key) {
		CachedResult result;
		try {
			result = cache.get(key);
		} catch (ExecutionException e) {
			throw new IllegalStateException("Error authenticating credentials", e);
		}
		return result.authorization;
	}

	protected CachedResult loadForCache(CacheKey key) {
		ProjectAuthorization authorization;
		if (key.token != null) {
			authorization = inner.validateToken(key.token, key.projectKey);
		} else {
			authorization = inner.validateChain(key.chain.buildCertificates(), key.projectKey);
		}

		// TODO: Different cache validity for "access denied"?
		return new CachedResult(authorization);
	}

	@Override
	public ProjectAuthorization validateChain(X509Certificate[] chain, String projectKey) {
		CacheKey key = new CacheKey(projectKey, chain);

		return authenticate(key);
	}

	public AuthenticationTokenValidator getInner() {
		return inner;
	}

	@Override
	public void discoverMetrics(MetricsSystem system) {
		MetricKey key = MetricKey.build(getClass(), "cache");
		system.add(new CacheMetricsReporter(key, cache));
		system.discoverMetrics(inner);
	}
}
