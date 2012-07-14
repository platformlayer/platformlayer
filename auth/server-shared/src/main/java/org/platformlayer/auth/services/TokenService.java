package org.platformlayer.auth.services;

public interface TokenService {
	TokenInfo decodeToken(String token);

	String encodeToken(TokenInfo token);
}
