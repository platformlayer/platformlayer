package org.platformlayer.auth.cache;

import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.platformlayer.auth.AuthenticationTokenValidator;
import org.platformlayer.model.AuthenticationToken;
import org.platformlayer.model.ProjectAuthorization;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class CachingAuthenticationTokenValidator implements AuthenticationTokenValidator {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(CachingAuthenticationTokenValidator.class);

	static final int CACHE_MAX_SIZE = 1000;
	static final int CACHE_VALIDITY_MINUTES = 5;

	final AuthenticationTokenValidator inner;

	public CachingAuthenticationTokenValidator(AuthenticationTokenValidator inner) {
		this.inner = inner;
	}

	static class CacheKey {
		final String projectKey;
		final AuthenticationToken token;
		final X509Certificate[] chain;

		CacheKey(String projectKey, AuthenticationToken token) {
			this.projectKey = projectKey;
			this.token = token;
			this.chain = null;
		}

		// TODO: The cert chain is probably quite memory hungry...
		CacheKey(String projectKey, X509Certificate[] chain) {
			this.projectKey = projectKey;
			this.token = null;
			this.chain = chain;
		}

	};

	static class CachedResult {
		final ProjectAuthorization authorization;

		public CachedResult(ProjectAuthorization authorization) {
			this.authorization = authorization;
		}

	};

	final LoadingCache<CacheKey, CachedResult> cache = CacheBuilder.newBuilder().maximumSize(CACHE_MAX_SIZE)
			.expireAfterWrite(CACHE_VALIDITY_MINUTES, TimeUnit.MINUTES)
			// .removalListener(MY_LISTENER)
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
			authorization = inner.validateChain(key.chain, key.projectKey);
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
}
