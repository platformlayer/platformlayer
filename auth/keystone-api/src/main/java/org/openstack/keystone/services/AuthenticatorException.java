package org.openstack.keystone.services;

public class AuthenticatorException extends Exception {

    private static final long serialVersionUID = 1L;

    public AuthenticatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthenticatorException(String message) {
        super(message);
    }

}
