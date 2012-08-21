package org.platformlayer.http.apache;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.platformlayer.http.HttpConfiguration;
import org.platformlayer.http.HttpStrategy;
import org.platformlayer.http.SslConfiguration;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

public abstract class CachingHttpStrategy implements HttpStrategy {
	private static final Logger log = Logger.getLogger(CachingHttpStrategy.class);

	private static final long MAX_SIZE = 32;

	HttpConfiguration defaultConfiguration;

	final LoadingCache<SslConfiguration, HttpConfiguration> cache = CacheBuilder.newBuilder().maximumSize(MAX_SIZE)
	// .expireAfterWrite(10, TimeUnit.MINUTES)
			.removalListener(new RemovalListener<SslConfiguration, HttpConfiguration>() {

				@Override
				public void onRemoval(RemovalNotification<SslConfiguration, HttpConfiguration> notification) {
					HttpConfiguration value = notification.getValue();
					try {
						log.info("Closing cached http configuration: " + value);
						value.close();
					} catch (IOException e) {
						log.warn("Unexpected error while closing HTTP configuration", e);
					}
				}
			}).build(new CacheLoader<SslConfiguration, HttpConfiguration>() {
				@Override
				public HttpConfiguration load(SslConfiguration key) throws Exception {
					return build0(key);
				}
			});

	@Override
	public final HttpConfiguration buildConfiguration(SslConfiguration sslConfiguration) {
		if (sslConfiguration == null || sslConfiguration.isEmpty()) {
			sslConfiguration = null;
		}

		if (sslConfiguration == null) {
			synchronized (this) {
				if (defaultConfiguration == null) {
					HttpConfiguration configuration = build0(sslConfiguration);
					defaultConfiguration = configuration;
				}
			}
			return defaultConfiguration;
		}

		try {
			return cache.get(sslConfiguration);
		} catch (ExecutionException e) {
			throw new IllegalArgumentException("Error building http configuration", e);
		}
	}

	protected abstract HttpConfiguration build0(SslConfiguration sslConfiguration);
}
