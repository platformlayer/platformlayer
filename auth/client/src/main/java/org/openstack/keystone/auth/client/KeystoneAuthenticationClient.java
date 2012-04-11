package org.openstack.keystone.auth.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;

import javax.xml.bind.JAXBException;

import org.openstack.docs.identity.api.v2.Auth;
import org.openstack.docs.identity.api.v2.AuthenticateRequest;
import org.openstack.docs.identity.api.v2.AuthenticateResponse;
import org.openstack.docs.identity.api.v2.PasswordCredentials;
import org.openstack.docs.identity.api.v2.TenantsList;
import org.openstack.utils.Utf8;
import org.platformlayer.CastUtils;
import org.platformlayer.IoUtils;
import org.platformlayer.WellKnownPorts;
import org.platformlayer.http.SimpleHttpRequest;
import org.platformlayer.http.SimpleHttpRequest.SimpleHttpResponse;
import org.platformlayer.xml.JaxbHelper;
import org.platformlayer.xml.UnmarshalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeystoneAuthenticationClient {
    static final Logger log = LoggerFactory.getLogger(KeystoneAuthenticationClient.class);

    final String authenticationUrl;

    public static final String DEFAULT_AUTHENTICATION_URL = "http://127.0.0.1:" + WellKnownPorts.PORT_PLATFORMLAYER_AUTH_USER + "/v2.0/";

    public static final Integer HTTP_500_ERROR = new Integer(500);

    protected static final int MAX_RETRIES = 10;

    static Random random = new Random();

    public KeystoneAuthenticationClient(String authenticationUrl) {
        this.authenticationUrl = authenticationUrl;
    }

    public KeystoneAuthenticationClient() {
        this(DEFAULT_AUTHENTICATION_URL);
    }

    public TenantsList listTenants(KeystoneAuthenticationToken token) throws KeystoneAuthenticationException {
        return doSimpleRequest(token, "GET", "tokens", null, TenantsList.class);
    }

    public KeystoneAuthenticationToken authenticate(String tenantName, PasswordCredentials passwordCredentials) throws KeystoneAuthenticationException {
        Auth auth = new Auth();
        auth.setPasswordCredentials(passwordCredentials);
        auth.setTenantName(tenantName);

        AuthenticateRequest request = new AuthenticateRequest();
        request.setAuth(auth);

        AuthenticateResponse response = doSimpleRequest(null, "POST", "tokens", request, AuthenticateResponse.class);
        return new KeystoneAuthenticationToken(response.getAccess());
    }

    private <T> T doSimpleRequest(KeystoneAuthenticationToken token, String method, String relativeUri, Object postObject, Class<T> responseClass) throws KeystoneAuthenticationException {
        try {
            URI uri = new URI(authenticationUrl + relativeUri);

            SimpleHttpRequest httpRequest = SimpleHttpRequest.build(method, uri);

            httpRequest.setRequestHeader("Accept", "application/xml");

            if (token != null) {
                token.populateRequest(httpRequest);
            }

            if (postObject != null) {
                httpRequest.setRequestHeader("Content-Type", "application/xml");
                String xml = serializeXml(postObject);
                httpRequest.getOutputStream().write(Utf8.getBytes(xml));
            }

            SimpleHttpResponse response = httpRequest.doRequest();

            int responseCode = response.getHttpResponseCode();
            switch (responseCode) {
            case 401:
                throw new KeystoneAuthenticationException("Platformlayer credentials were not correct");

            case 200:
            case 203: {
                if (responseClass.equals(String.class)) {
                    return CastUtils.as(IoUtils.readAll(response.getInputStream()), responseClass);
                } else {
                    return deserializeXml(response.getInputStream(), responseClass);
                }
            }

            default:
                throw new KeystoneAuthenticationException("Unexpected result code: " + responseCode);
            }
        } catch (IOException e) {
            throw new KeystoneAuthenticationException("Error communicating with authentication service", e);
        } catch (URISyntaxException e) {
            throw new KeystoneAuthenticationException("Error building authentication URI", e);
        }

    }

    public static <T> T deserializeXml(InputStream is, Class<T> clazz) throws KeystoneAuthenticationException {
        try {
            return JaxbHelper.deserializeXmlObject(is, clazz, true);
        } catch (UnmarshalException e) {
            throw new KeystoneAuthenticationException("Error reading authentication response data", e);
        }
    }

    public static String serializeXml(Object object) throws KeystoneAuthenticationException {
        try {
            boolean formatted = false;
            return JaxbHelper.toXml(object, formatted);
        } catch (JAXBException e) {
            throw new KeystoneAuthenticationException("Error serializing data", e);
        }
    }

}
