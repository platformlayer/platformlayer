package org.openstack.keystone.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.openstack.keystone.auth.client.KeystoneAuthenticationException;
import org.openstack.utils.Utf8;
import org.platformlayer.CastUtils;
import org.platformlayer.IoUtils;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.http.SimpleHttpRequest;
import org.platformlayer.http.SimpleHttpRequest.SimpleHttpResponse;
import org.platformlayer.xml.JaxbHelper;
import org.platformlayer.xml.UnmarshalException;

public class RestfulClient {
    static final Logger log = Logger.getLogger(RestfulClient.class);

    final String baseUrl;

    public RestfulClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    protected <T> T doSimpleRequest(String method, String relativeUri, Object postObject, Class<T> responseClass) throws PlatformLayerClientException {
        try {
            URI uri = new URI(baseUrl + relativeUri);

            log.info("HTTP Request: " + method + " " + uri);

            SimpleHttpRequest httpRequest = SimpleHttpRequest.build(method, uri);
            httpRequest.setRequestHeader("Accept", "application/xml");

            addHeaders(httpRequest);

            if (postObject != null) {
                httpRequest.setRequestHeader("Content-Type", "application/xml");
                String xml = serializeXml(postObject);
                httpRequest.getOutputStream().write(Utf8.getBytes(xml));
            }

            SimpleHttpResponse response = httpRequest.doRequest();

            int responseCode = response.getHttpResponseCode();
            switch (responseCode) {
            case 401:
                throw new KeystoneAuthenticationException("Authentication failure");

            case 200:
            case 203: {
                if (responseClass.equals(String.class)) {
                    return CastUtils.as(IoUtils.readAll(response.getInputStream()), responseClass);
                } else {
                    return deserializeXml(response.getInputStream(), responseClass);
                }
            }

            default:
                throw new PlatformLayerClientException("Unexpected result code: " + responseCode, null, responseCode);
            }
        } catch (IOException e) {
            throw new KeystoneAuthenticationException("Error communicating with service", e);
        } catch (URISyntaxException e) {
            throw new KeystoneAuthenticationException("Error building URI", e);
        }

    }

    protected void addHeaders(SimpleHttpRequest httpRequest) {

    }

    <T> T deserializeXml(InputStream is, Class<T> clazz) throws KeystoneAuthenticationException {
        try {
            return JaxbHelper.deserializeXmlObject(is, clazz, true);
        } catch (UnmarshalException e) {
            throw new KeystoneAuthenticationException("Error reading authentication response data", e);
        }
    }

    String serializeXml(Object object) throws KeystoneAuthenticationException {
        try {
            boolean formatted = false;
            return JaxbHelper.toXml(object, formatted);
        } catch (JAXBException e) {
            throw new KeystoneAuthenticationException("Error serializing data", e);
        }
    }
}
