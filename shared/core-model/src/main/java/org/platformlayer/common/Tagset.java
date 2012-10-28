package org.platformlayer.common;

import java.util.List;

public interface Tagset extends Iterable<IsTag> {
	String findFirst(String key);

	@Deprecated
	String findUnique(String key);

	List<String> findAll(String key);

	void add(IsTag tag);

	void addAll(Iterable<? extends IsTag> tags);
	// HasTags getTags();
}
