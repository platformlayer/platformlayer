package org.platformlayer.auth.client;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.crypto.SecretKey;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import org.openstack.crypto.CertificateAndKey;
import org.openstack.crypto.Md5Hash;
import org.platformlayer.WellKnownPorts;
import org.platformlayer.auth.AuthenticationTokenValidator;
import org.platformlayer.auth.PlatformlayerProjectAuthorization;
import org.platformlayer.auth.PlatformlayerUserAuthentication;
import org.platformlayer.auth.cache.CachingAuthenticationTokenValidator;
import org.platformlayer.auth.v1.CertificateChainInfo;
import org.platformlayer.auth.v1.CertificateInfo;
import org.platformlayer.auth.v1.ProjectValidation;
import org.platformlayer.auth.v1.SignCertificateRequest;
import org.platformlayer.auth.v1.SignCertificateResponse;
import org.platformlayer.auth.v1.UserValidation;
import org.platformlayer.auth.v1.ValidateAccess;
import org.platformlayer.auth.v1.ValidateTokenResponse;
import org.platformlayer.config.Configuration;
import org.platformlayer.crypto.AcceptAllHostnameVerifier;
import org.platformlayer.crypto.AesUtils;
import org.platformlayer.crypto.CertificateUtils;
import org.platformlayer.crypto.EncryptionStore;
import org.platformlayer.crypto.OpenSshUtils;
import org.platformlayer.crypto.PublicKeyTrustManager;
import org.platformlayer.crypto.SimpleClientCertificateKeyManager;
import org.platformlayer.http.HttpStrategy;
import org.platformlayer.http.SslConfiguration;
import org.platformlayer.model.AuthenticationToken;
import org.platformlayer.model.ProjectAuthorization;
import org.platformlayer.ops.OpsException;
import org.platformlayer.rest.HttpUtils;
import org.platformlayer.rest.JreRestfulClient;
import org.platformlayer.rest.RestClientException;
import org.platformlayer.rest.RestfulClient;
import org.platformlayer.rest.RestfulRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

		String cert = configuration.get("auth.system.ssl.cert");
		// String secret = configuration.lookup("multitenant.cert.password", KeyStoreUtils.DEFAULT_KEYSTORE_SECRET);

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

		url += "?project=" + HttpUtils.urlEncode(projectId);

		try {
			ValidateTokenResponse response = doSimpleRequest("GET", url, null, ValidateTokenResponse.class);

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
			PlatformlayerProjectAuthorization project = new PlatformlayerProjectAuthorization(user, projectInfo);
			return project;
		} catch (RestClientException e) {
			if (e.getHttpResponseCode() != null && e.getHttpResponseCode() == 404) {
				// Not found => invalid token
				return null;
			}
			throw new IllegalArgumentException("Error while validating token", e);
		}
	}

	@Override
	public ProjectAuthorization validateChain(X509Certificate[] chain, String projectKey) {
		// v2.0/keychain[?project={projectKey}]

		String url = "v2.0/keychain";

		url += "?project=" + HttpUtils.urlEncode(projectKey);

		CertificateChainInfo chainInfo = new CertificateChainInfo();
		List<CertificateInfo> certificates = chainInfo.getCertificates();
		for (X509Certificate cert : chain) {
			CertificateInfo certificateInfo = new CertificateInfo();
			Md5Hash hash = OpenSshUtils.getSignature(cert.getPublicKey());
			certificateInfo.setPublicKeyHash(hash.toHex());
			certificates.add(certificateInfo);
		}

		try {
			ValidateTokenResponse response = doSimpleRequest("POST", url, chainInfo, ValidateTokenResponse.class);

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
			PlatformlayerProjectAuthorization project = new PlatformlayerProjectAuthorization(user, projectInfo);
			return project;
		} catch (RestClientException e) {
			if (e.getHttpResponseCode() != null && e.getHttpResponseCode() == 404) {
				// Not found => invalid token
				return null;
			}
			throw new IllegalArgumentException("Error while validating credentials", e);
		}
	}

	// This can actually be moved to the user-auth system
	public List<X509Certificate> signCsr(String projectKey, SecretKey projectSecret, String csr) {
		String url = "pki/csr";

		SignCertificateRequest request = new SignCertificateRequest();
		request.setProject(projectKey);
		request.setCsr(csr);
		request.setProjectSecret(AesUtils.serialize(projectSecret));

		try {
			SignCertificateResponse response = doSimpleRequest("POST", url, request, SignCertificateResponse.class);

			List<X509Certificate> certificates = Lists.newArrayList();
			for (String cert : response.getCertificates()) {
				certificates.addAll(CertificateUtils.fromPem(cert));
			}

			return certificates;
		} catch (RestClientException e) {
			throw new IllegalArgumentException("Error while signing certificate", e);
		}
	}

	protected <T> T doSimpleRequest(String method, String relativeUri, Object postObject, Class<T> responseClass)
			throws RestClientException {
		RestfulRequest<T> request = restfulClient.buildRequest(method, relativeUri, postObject, responseClass);
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

}
