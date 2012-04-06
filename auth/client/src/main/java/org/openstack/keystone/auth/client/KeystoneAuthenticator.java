package org.openstack.keystone.auth.client;

import java.net.MalformedURLException;
import java.net.URL;

import org.openstack.docs.identity.api.v2.PasswordCredentials;
import org.platformlayer.auth.AuthenticationToken;
import org.platformlayer.auth.Authenticator;

public class KeystoneAuthenticator implements Authenticator {
    final String tenantId;

    final String username;
    final String password;

    final KeystoneAuthenticationClient client;

    AuthenticationToken token = null;

    public KeystoneAuthenticator(String tenantId, String username, String password, String server) {
        this.tenantId = tenantId;
        this.username = username;
        this.password = password;
        String authenticationUrl = server != null ? server : KeystoneAuthenticationClient.DEFAULT_AUTHENTICATION_URL;

        this.client = new KeystoneAuthenticationClient(authenticationUrl);
    }

    @Override
    public AuthenticationToken getAuthenticationToken() throws KeystoneAuthenticationException {
        if (token == null) {
            PasswordCredentials passwordCredentials = new PasswordCredentials();
            passwordCredentials.setUsername(username);
            passwordCredentials.setPassword(password);

            token = client.authenticate(tenantId, passwordCredentials);
        }
        return token;
    }

    @Override
    public void clearAuthenticationToken() {
        token = null;
    }

    @Override
    public String getHost() {
        try {
            URL url = new URL(client.authenticationUrl);
            return url.getHost();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Error parsing URL", e);
        }
    }

}
