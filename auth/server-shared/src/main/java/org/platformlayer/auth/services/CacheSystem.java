package org.platformlayer.auth.services;

import java.io.Serializable;

import com.fathomdb.TimeSpan;

public interface CacheSystem {
	<T> T lookup(String key, Class<T> clazz);

	void put(String key, TimeSpan validity, Serializable value);
}
