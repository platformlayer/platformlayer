package org.openstack.keystone.services;

public interface TokenService {
    TokenInfo decodeToken(boolean system, String token);

    String encodeToken(TokenInfo token);
}
