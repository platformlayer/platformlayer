package org.openstack.keystone.services;

public interface TokenService {
	TokenInfo decodeToken(String token);

	String encodeToken(TokenInfo token);
}
