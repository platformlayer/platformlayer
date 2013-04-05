package org.platformlayer.auth.system;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.inject.Inject;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import org.platformlayer.WellKnownPorts;
import org.platformlayer.auth.AuthenticationToken;
import org.platformlayer.auth.AuthenticationTokenValidator;
import org.platformlayer.auth.PlatformlayerAuthenticationToken;
import org.platformlayer.auth.v1.CertificateChainInfo;
import org.platformlayer.auth.v1.CheckServiceAccessRequest;
import org.platformlayer.auth.v1.CheckServiceAccessResponse;
import org.platformlayer.auth.v1.ProjectValidation;
import org.platformlayer.auth.v1.Role;
import org.platformlayer.auth.v1.SignCertificateRequest;
import org.platformlayer.auth.v1.SignCertificateResponse;
import org.platformlayer.auth.v1.UserValidation;
import org.platformlayer.auth.v1.ValidateAccess;
import org.platformlayer.auth.v1.ValidateTokenResponse;
import org.platformlayer.crypto.CertificateUtils;
import org.platformlayer.http.HttpMethod;
import org.platformlayer.http.HttpStrategy;
import org.platformlayer.http.SslConfiguration;
import org.platformlayer.http.UrlUtils;
import org.platformlayer.model.ProjectAuthorization;
import org.platformlayer.model.RoleId;
import org.platformlayer.ops.OpsException;
import org.platformlayer.rest.HttpPayload;
import org.platformlayer.rest.JreRestfulClient;
import org.platformlayer.rest.RestClientException;
import org.platformlayer.rest.RestfulClient;
import org.platformlayer.rest.RestfulRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.Configuration;
import com.fathomdb.crypto.CertificateAndKey;
import com.fathomdb.crypto.CryptoKey;
import com.fathomdb.crypto.EncryptionStore;
import com.fathomdb.crypto.FathomdbCrypto;
import com.fathomdb.crypto.SimpleClientCertificateKeyManager;
import com.fathomdb.crypto.ssl.AcceptAllHostnameVerifier;
import com.fathomdb.crypto.ssl.PublicKeyTrustManager;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class PlatformLayerAuthAdminClient implements AuthenticationTokenValidator {
	static final Logger log = LoggerFactory.getLogger(PlatformLayerAuthAdminClient.class);

	public static final String DEFAULT_AUTHENTICATION_URL = "https://127.0.0.1:"
			+ WellKnownPorts.PORT_PLATFORMLAYER_AUTH_ADMIN + "/";

	final RestfulClient restfulClient;

	private PlatformLayerAuthAdminClient(RestfulClient restfulClient) {
		this.restfulClient = restfulClient;
	}

	public static AuthenticationTokenValidator build(HttpStrategy httpStrategy, Configuration configuration,
			EncryptionStore encryptionStore) throws OpsException {
		String keystoneServiceUrl = configuration.lookup("auth.system.url", "https://127.0.0.1:"
				+ WellKnownPorts.PORT_PLATFORMLAYER_AUTH_ADMIN + "/");

		String cert = configuration.get("auth.system.tls.clientcert");

		CertificateAndKey certificateAndKey = encryptionStore.getCertificateAndKey(cert);

		HostnameVerifier hostnameVerifier = null;

		KeyManager keyManager = new SimpleClientCertificateKeyManager(certificateAndKey);

		TrustManager trustManager = null;

		String trustKeys = configuration.lookup("auth.system.ssl.keys", null);

		if (trustKeys != null) {
			trustManager = new PublicKeyTrustManager(Splitter.on(',').trimResults().split(trustKeys));

			hostnameVerifier = new AcceptAllHostnameVerifier();
		}

		SslConfiguration sslConfiguration = new SslConfiguration(keyManager, trustManager, hostnameVerifier);
		RestfulClient restfulClient = new JreRestfulClient(httpStrategy, keystoneServiceUrl, sslConfiguration);

		AuthenticationTokenValidator tokenValidator = new PlatformLayerAuthAdminClient(restfulClient);
		tokenValidator = new CachingAuthenticationTokenValidator(tokenValidator);
		return tokenValidator;
	}

	@Override
	public ProjectAuthorization validateToken(AuthenticationToken authToken, String projectId) {
		// v2.0/tokens/{userToken}[?project={tenant}]

		String tokenId = ((PlatformlayerAuthenticationToken) authToken).getAuthTokenValue();
		tokenId = tokenId.trim();

		String url = "v2.0/tokens/" + tokenId;

		url += "?project=" + UrlUtils.urlEncode(projectId);

		try {
			ValidateTokenResponse response = doSimpleXmlRequest(HttpMethod.GET, url, null, ValidateTokenResponse.class);

			ValidateAccess access = response.getAccess();
			if (access == null) {
				return null;
			}

			// ProjectValidation project = access.getProject();
			// if (project == null || !Objects.equal(projectId, project.getId())) {
			// return null;
			// }

			UserValidation userInfo = access.getUser();
			if (userInfo == null) {
				return null;
			}

			ProjectValidation projectInfo = access.getProject();
			if (projectInfo == null) {
				return null;
			}

			// List<String> roles = Lists.newArrayList();
			// UserValidation userInfo = access.getUser();
			// for (Role role : userInfo.getRoles()) {
			// if (!role.getTenantId().equals(projectId)) {
			// throw new IllegalStateException("Tenant mismatch: " + role.getTenantId() + " vs " + projectId);
			// }
			// roles.add(role.getName());
			// }

			// byte[] userSecret = userInfo.getSecret();
			String userKey = userInfo.getName();

			PlatformlayerUserAuthentication user = new PlatformlayerUserAuthentication(authToken, userKey);
			PlatformlayerProjectAuthorization project = buildPlatformlayerProjectAuthorization(user, projectInfo);
			return project;
		} catch (RestClientException e) {
			if (e.getHttpResponseCode() != null && e.getHttpResponseCode() == 404) {
				// Not found => invalid token
				return null;
			}
			log.warn("Error while validating token", e);
			throw new IllegalArgumentException("Error while validating token", e);
		}
	}

	@Override
	public ProjectAuthorization validateChain(X509Certificate[] chain, String projectKey) {
		// v2.0/keychain[?project={projectKey}]

		String url = "v2.0/keychain";

		url += "?project=" + UrlUtils.urlEncode(projectKey);

		CertificateChainInfo chainInfo = CertificateChains.toModel(chain);

		try {
			ValidateTokenResponse response = doSimpleXmlRequest(HttpMethod.POST, url, chainInfo,
					ValidateTokenResponse.class);

			ValidateAccess access = response.getAccess();
			if (access == null) {
				return null;
			}

			UserValidation userInfo = access.getUser();
			if (userInfo == null) {
				return null;
			}

			ProjectValidation projectInfo = access.getProject();
			if (projectInfo == null) {
				return null;
			}

			String userKey = userInfo.getName();

			PlatformlayerUserAuthentication user = new PlatformlayerUserAuthentication(null, userKey);
			PlatformlayerProjectAuthorization project = buildPlatformlayerProjectAuthorization(user, projectInfo);
			return project;
		} catch (RestClientException e) {
			if (e.getHttpResponseCode() != null && e.getHttpResponseCode() == 404) {
				// Not found => invalid token
				return null;
			}
			log.warn("Error while validating credentials", e);
			throw new IllegalArgumentException("Error while validating credentials", e);
		}
	}

	private PlatformlayerProjectAuthorization buildPlatformlayerProjectAuthorization(
			PlatformlayerUserAuthentication user, ProjectValidation project) {

		String name = project.getName();
		int projectId = Integer.parseInt(project.getId());

		List<RoleId> roles = Lists.newArrayList();
		for (Role role : project.getRoles()) {
			roles.add(new RoleId(role.getName()));
		}

		CryptoKey projectSecret = FathomdbCrypto.deserializeKey(project.getSecret());
		return new PlatformlayerProjectAuthorization(user, name, projectSecret, roles, projectId);
	}

	// This can actually be moved to the user-auth system
	public List<X509Certificate> signCsr(String projectKey, CryptoKey projectSecret, String csr) {
		String url = "pki/csr";

		SignCertificateRequest request = new SignCertificateRequest();
		request.setProject(projectKey);
		request.setCsr(csr);
		request.setProjectSecret(FathomdbCrypto.serialize(projectSecret));

		try {
			SignCertificateResponse response = doSimpleXmlRequest(HttpMethod.POST, url, request,
					SignCertificateResponse.class);

			List<X509Certificate> certificates = Lists.newArrayList();
			for (String cert : response.getCertificates()) {
				certificates.addAll(CertificateUtils.fromPem(cert));
			}

			return certificates;
		} catch (RestClientException e) {
			throw new IllegalArgumentException("Error while signing certificate", e);
		}
	}

	public String checkServiceAccess(CertificateChainInfo chain) {
		String url = "services/check";

		CheckServiceAccessRequest request = new CheckServiceAccessRequest();
		request.setChain(chain);

		try {
			CheckServiceAccessResponse response = doSimpleXmlRequest(HttpMethod.POST, url, request,
					CheckServiceAccessResponse.class);

			return response.getServiceAccount();
		} catch (RestClientException e) {
			throw new IllegalArgumentException("Error while checking service access", e);
		}
	}

	protected <T> T doSimpleXmlRequest(HttpMethod method, String relativeUri, Object postObject, Class<T> responseClass)
			throws RestClientException {
		HttpPayload payload = postObject != null ? HttpPayload.asXml(postObject) : null;
		RestfulRequest<T> request = restfulClient.buildRequest(method, relativeUri, payload, responseClass);
		return request.execute();
	}

	public static PlatformLayerAuthAdminClient find(AuthenticationTokenValidator authenticationTokenValidator) {
		if (authenticationTokenValidator instanceof PlatformLayerAuthAdminClient) {
			return (PlatformLayerAuthAdminClient) authenticationTokenValidator;
		}
		if (authenticationTokenValidator instanceof CachingAuthenticationTokenValidator) {
			return find(((CachingAuthenticationTokenValidator) authenticationTokenValidator).getInner());
		}
		throw new IllegalArgumentException();
	}

	public static class Provider implements javax.inject.Provider<AuthenticationTokenValidator> {
		@Inject
		HttpStrategy httpStrategy;

		@Inject
		Configuration configuration;

		@Inject
		EncryptionStore encryptionStore;

		@Override
		public AuthenticationTokenValidator get() {
			try {
				return build(httpStrategy, configuration, encryptionStore);
			} catch (OpsException e) {
				throw new IllegalStateException("Error building AuthenticationTokenValidator", e);
			}
		}

	}

}
