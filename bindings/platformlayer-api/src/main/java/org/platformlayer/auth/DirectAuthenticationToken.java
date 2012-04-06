package org.platformlayer.auth;

import javax.crypto.SecretKey;

import org.platformlayer.DirectPlatformLayerClient;
import org.platformlayer.crypto.CryptoUtils;
import org.platformlayer.http.SimpleHttpRequest;

import com.google.common.base.Objects;

public class DirectAuthenticationToken implements AuthenticationToken {
    private final String serviceUrl;
    // private final Mac signingKey;
    private final String keyId;
    private final SecretKey secret;

    public DirectAuthenticationToken(String serviceUrl, String keyId, SecretKey secret) {
        this.serviceUrl = serviceUrl;
        this.keyId = keyId;
        this.secret = secret;

        // this.signingKey = AuthenticationSignature.buildMac(secret);
    }

    @Override
    public String getServiceUrl(String serviceKey) {
        if (Objects.equal(DirectPlatformLayerClient.SERVICE_PLATFORMLAYER, serviceKey)) {
            return serviceUrl;
        }
        return null;
    }

    @Override
    public void populateRequest(SimpleHttpRequest httpRequest) {
        // String method = httpRequest.getMethod();
        // String requestPath = httpRequest.getUrl().getPath();
        // String timestamp = String.valueOf(System.currentTimeMillis());
        //
        // byte[] signature = AuthenticationSignature.calculateSignature(signingKey, timestamp, method, requestPath);
        //
        // httpRequest.setRequestHeader("X-Timestamp", timestamp);
        // httpRequest.setRequestHeader("X-Auth-Key", keyId);
        // httpRequest.setRequestHeader("X-Auth-Signed", CryptoUtils.toBase64(signature));

        httpRequest.setRequestHeader("X-Auth-Key", keyId);
        httpRequest.setRequestHeader("X-Auth-Secret", CryptoUtils.toBase64(secret.getEncoded()));

    }
}
