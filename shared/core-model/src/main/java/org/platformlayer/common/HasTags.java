package org.platformlayer.common;

public interface HasTags {
	String findFirst(String key);

	@Deprecated
	String findUnique(String key);

	Iterable<String> findAll(String key);
}
