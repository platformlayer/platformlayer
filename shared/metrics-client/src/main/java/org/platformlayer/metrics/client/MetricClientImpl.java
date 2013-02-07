package org.platformlayer.metrics.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DecompressingHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.platformlayer.http.SslHelpers;
import org.platformlayer.metrics.MetricTreeObject;
import org.platformlayer.metrics.model.MetricQuery;
import org.platformlayer.ops.OpsException;
import org.platformlayer.rest.RestClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.Configuration;
import com.fathomdb.crypto.CertificateAndKey;
import com.fathomdb.crypto.EncryptionStore;
import com.fathomdb.crypto.SimpleClientCertificateKeyManager;
import com.fathomdb.crypto.ssl.PublicKeyTrustManager;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

@Singleton
public class MetricClientImpl implements MetricClient {
	public static class Provider implements com.google.inject.Provider<MetricClient> {
		@Inject
		Configuration configuration;

		@Inject
		EncryptionStore encryptionStore;

		@Override
		public MetricClient get() {
			try {
				return MetricClientImpl.build(configuration, encryptionStore);
			} catch (OpsException e) {
				throw new IllegalStateException("Error building metric client", e);
			}
		}
	}

	private static final Logger log = LoggerFactory.getLogger(MetricClientImpl.class);

	final HttpClient httpClient;
	final URI metricBaseUrl;
	final MetricTreeSerializer metricTreeSerializer;

	final String project;

	final MetricTreeObject tags;

	public MetricClientImpl(URI metricBaseUrl, String project, MetricTreeObject tags,
			CertificateAndKey certificateAndKey, List<String> trustKeys) {
		super();
		this.metricBaseUrl = metricBaseUrl;
		this.project = project;
		this.tags = tags;
		this.metricTreeSerializer = new MetricTreeSerializer();

		this.httpClient = buildHttpClient(certificateAndKey, trustKeys);
	}

	@Override
	public void close() {
		httpClient.getConnectionManager().shutdown();
	}

	private HttpClient buildHttpClient(CertificateAndKey certificateAndKey, List<String> trustKeys) {
		int port = metricBaseUrl.getPort();
		if (port == -1) {
			String scheme = metricBaseUrl.getScheme();
			if (scheme.equals("https")) {
				port = 443;
			} else if (scheme.equals("http")) {
				port = 80;
			} else {
				throw new IllegalArgumentException("Unknown scheme: " + scheme);
			}
		}

		SchemeSocketFactory schemeSocketFactory;
		try {
			KeyManager keyManager = new SimpleClientCertificateKeyManager(certificateAndKey);

			TrustManager trustManager;
			X509HostnameVerifier hostnameVerifier;
			if (trustKeys != null) {
				trustManager = new PublicKeyTrustManager(trustKeys);
				hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
			} else {
				trustManager = null;
				hostnameVerifier = SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER;
			}
			javax.net.ssl.SSLSocketFactory sslSocketFactory = SslHelpers
					.buildSslSocketFactory(keyManager, trustManager);

			schemeSocketFactory = new SSLSocketFactory(sslSocketFactory, hostnameVerifier);
		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException("Error building SSL client", e);
		}

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("https", port, schemeSocketFactory));

		PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager(schemeRegistry);

		HttpClient httpClient = new DefaultHttpClient(connectionManager);

		httpClient = new DecompressingHttpClient(httpClient);

		return httpClient;
	}

	// TODO: Throw on failure??
	@Override
	public boolean sendMetrics(MetricTreeObject tree) {
		if (tags != null) {
			tree.mergeTree(tags);
		}

		URI url = metricBaseUrl.resolve("api/metric/").resolve(project);
		HttpPost request = new HttpPost(url);
		HttpResponse response = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			metricTreeSerializer.serialize(tree, baos);
			baos.close();

			byte[] data = baos.toByteArray();

			log.debug("POSTing " + new String(data));

			// TODO: Stream body? We'd just need a custom ByteArrayEntity class
			request.setEntity(new ByteArrayEntity(data));

			response = httpClient.execute(request);

			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() != 200) {
				log.warn("Error writing to PlatformLayer metrics server: " + statusLine);
				return false;
			} else {
				EntityUtils.consume(response.getEntity());
				response = null;
				// consumeResponse(request, response);
				log.debug("Posted metrics");
				return true;
			}
		} catch (IOException e) {
			if (log.isDebugEnabled()) {
				log.debug("Error writing to PlatformLayer metrics server", e);
			}
			log.warn("Error writing to PlatformLayer metrics server {}", e.getMessage());
			return false;
		} finally {
			if (response != null) {
				try {
					EntityUtils.consume(response.getEntity());
				} catch (IOException e) {
					log.warn("Error consuming response", e);
				}
			}
		}
	}

	@Override
	public MetricServiceData getMetrics(MetricQuery query) throws RestClientException {
		URI url = metricBaseUrl.resolve("api/metric/").resolve(project + "/");

		URIBuilder uriBuilder = new URIBuilder(url);

		if (query != null) {
			for (String filter : query.filters) {
				int firstEquals = filter.indexOf('=');
				if (firstEquals == -1) {
					uriBuilder.addParameter("has." + filter, "");
				} else {
					String key = filter.substring(0, firstEquals);
					String value = filter.substring(firstEquals + 1);

					uriBuilder.addParameter("filter." + key, value);
				}
			}

			for (String projection : query.projections) {
				int firstEquals = projection.indexOf('=');
				if (firstEquals == -1) {
					uriBuilder.addParameter("select." + projection, "");
				} else {
					throw new IllegalArgumentException();
				}
			}

			if (query.flatten) {
				uriBuilder.addParameter("flatten", "true");
			}
		}

		try {
			url = uriBuilder.build();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Error building URI", e);
		}

		HttpGet request = new HttpGet(url);
		HttpResponse response = null;

		try {
			response = httpClient.execute(request);

			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() != 200) {
				log.info("Error reading from metrics service: " + statusLine);

				throw new RestClientException("Error reading from metrics service", null, statusLine.getStatusCode());
			} else {
				MetricServiceData ret = new MetricServiceData(request, response);

				response = null; // Don't close yet

				return ret;
			}
		} catch (IOException e) {
			throw new RestClientException("Error reading from metrics service", e);
		} finally {
			if (response != null) {
				try {
					EntityUtils.consume(response.getEntity());
				} catch (IOException e) {
					log.warn("Error consuming response", e);
				}
			}
		}
	}

	public static MetricClient build(Configuration configuration, EncryptionStore encryptionStore) throws OpsException {
		if (!configuration.lookup("metrics.report.enabled", true)) {
			return new DummyMetricClient();
		}

		String cert = configuration.get("metrics.report.ssl.cert");
		CertificateAndKey certificateAndKey = encryptionStore.getCertificateAndKey(cert);

		String project = configuration.get("metrics.report.project");

		MetricTreeObject tags = new MetricTreeObject(null);
		Map<String, String> tagProperties = configuration.getChildProperties("metrics.report.tags.");
		copyPropertiesToTree(tagProperties, tags.getSubtree("tags"));

		return build(configuration, encryptionStore, project, tags, certificateAndKey);
	}

	private static void copyPropertiesToTree(Map<String, String> properties, MetricTreeObject dest) {
		for (Entry<String, String> entry : properties.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			List<String> tokens = Lists.newArrayList(Splitter.on('.').split(key));

			MetricTreeObject subtree = dest;
			for (int i = 0; i < tokens.size() - 1; i++) {
				subtree = subtree.getSubtree(tokens.get(i));
			}

			String valueKey = tokens.get(tokens.size() - 1);

			subtree.addString(valueKey, value);
		}
	}

	public static MetricClientImpl build(Configuration configuration, EncryptionStore encryptionStore, String project,
			MetricTreeObject tags, CertificateAndKey certificateAndKey) throws OpsException {
		String metricBaseUrl = configuration.lookup("metrics.report.url", "https://metrics.platformlayer.net:8099/");

		String trustKeysString = configuration.lookup("metrics.report.ssl.keys", null);

		List<String> trustKeys = null;
		if (trustKeysString != null) {
			trustKeys = Lists.newArrayList(Splitter.on(',').trimResults().split(trustKeysString));
		}

		URI uri = URI.create(metricBaseUrl);

		MetricClientImpl metricSender = new MetricClientImpl(uri, project, tags, certificateAndKey, trustKeys);
		return metricSender;
	}

}
