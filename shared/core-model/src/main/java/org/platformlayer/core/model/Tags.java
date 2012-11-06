package org.platformlayer.core.model;

import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.common.TagsetHelpers;

import com.google.common.collect.Lists;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class Tags /* implements Tagset */implements Iterable<Tag> {
	/**
	 * Tags in this collection.
	 * 
	 * This should be List<IsTag>, but then JAXB can't serialize it.
	 */
	public List<Tag> tags = Lists.newArrayList();

	// @Override
	public final List<Tag> getTags() {
		return tags;
	}

	// @Override
	public void add(Tag tag) {
		tags.add(tag);
	}

	public boolean isEmpty() {
		return getTags().isEmpty();
	}

	@Override
	public Iterator<Tag> iterator() {
		return tags.iterator();
	}

	public List<String> findAll(String key) {
		return TagsetHelpers.findAll(tags, key);
	}

	public List<Tag> findTags(String key) {
		return TagsetHelpers.findTags(tags, key);
	}

	public boolean remove(Tag removeTag) {
		return getTags().remove(removeTag);
	}

	public String findUnique(String key) {
		return TagsetHelpers.findUnique(tags, key);
	}

	public Tag findUniqueTag(String key) {
		return TagsetHelpers.findUniqueTag(tags, key);
	}

	public boolean hasTag(String key, String value) {
		return hasTag(Tag.build(key, value));
	}

	public boolean hasTag(Tag find) {
		return TagsetHelpers.hasTag(tags, find);
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
		return "Tags [tags=" + getTags() + "]";
	}

	public String findFirst(String key) {
		return TagsetHelpers.findFirst(getTags(), key);
	}

	public static Tags build() {
		return new Tags();
	}
}
