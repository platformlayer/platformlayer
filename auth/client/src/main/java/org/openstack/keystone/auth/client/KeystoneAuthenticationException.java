package org.openstack.keystone.auth.client;

import org.platformlayer.auth.OpenstackAuthenticationException;

public class KeystoneAuthenticationException extends OpenstackAuthenticationException {
    private static final long serialVersionUID = 1L;

    public KeystoneAuthenticationException(String message) {
        super(message);
    }

    public KeystoneAuthenticationException(String message, Exception e) {
        super(message, e);
    }
}
