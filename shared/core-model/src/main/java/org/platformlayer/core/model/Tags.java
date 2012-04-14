package org.platformlayer.core.model;

import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class Tags implements Iterable<Tag> {
	static final Logger log = Logger.getLogger(Tags.class);

	public List<Tag> tags = Lists.newArrayList();

	public void add(String key, String value) {
		Tag tag = new Tag();
		tag.key = key;
		tag.value = value;
		tags.add(tag);
	}

	public void add(Tag tag) {
		tags.add(tag);
	}

	public boolean isEmpty() {
		return tags.isEmpty();
	}

	@Override
	public Iterator<Tag> iterator() {
		return tags.iterator();
	}

	public List<String> find(String key) {
		List<String> matches = Lists.newArrayList();
		for (Tag tag : tags) {
			if (key.equals(tag.getKey())) {
				matches.add(tag.getValue());
			}
		}
		return matches;
	}

	public List<Tag> findTags(String key) {
		List<Tag> matches = Lists.newArrayList();
		for (Tag tag : tags) {
			if (key.equals(tag.getKey())) {
				matches.add(tag);
			}
		}
		return matches;
	}

	public boolean remove(Tag removeTag) {
		return tags.remove(removeTag);
	}

	public String findUnique(String key) {
		Tag tag = findUniqueTag(key);
		if (tag == null) {
			return null;
		}
		return tag.getValue();
	}

	public Tag findUniqueTag(String key) {
		List<Tag> matches = findTags(key);
		if (matches.size() == 0) {
			return null;
		}
		if (matches.size() > 1) {
			// throw new IllegalStateException("Found duplicate tags for key: " + key);
			log.warn("Found duplicate tags for key: " + key + " matches=" + Joiner.on(',').join(matches));
		}
		return matches.get(0);
	}

	public boolean hasTag(String key, String value) {
		return hasTag(new Tag(key, value));
	}

	public boolean hasTag(Tag find) {
		for (Tag tag : tags) {
			if (tag.equals(find)) {
				return true;
			}
		}
		return false;
	}

	public void addAll(Iterable<Tag> tags) {
		if (tags != null) {
			for (Tag tag : tags) {
				add(tag);
			}
		}
	}

	@Override
	public String toString() {
		return "Tags [tags=" + tags + "]";
	}
}
