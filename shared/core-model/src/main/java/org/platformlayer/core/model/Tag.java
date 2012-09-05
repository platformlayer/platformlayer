package org.platformlayer.core.model;

import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;

@XmlAccessorType(XmlAccessType.FIELD)
public class Tag {
	public String key;
	public String value;

	public static final String OWNER_ID = "owner";

	// public static final String PLATFORM_LAYER_ID = "platformlayerid";

	@XmlTransient
	public abstract static class TagKey<T> {
		final String key;

		public TagKey(String key) {
			this.key = key;
		}

		public String getKey() {
			return key;
		}

		public T findUnique(ItemBase item) {
			return findUnique(item.getTags());
		}

		public List<T> find(ItemBase item) {
			return find(item.getTags());
		}

		public T findUnique(Tags tags) {
			String s = tags.findUnique(key);
			if (s == null) {
				return null;
			}
			return toT(s);
		}

		public Tag findUniqueTag(Tags tags) {
			return tags.findUniqueTag(key);
		}

		public Tag findUniqueTag(ItemBase item) {
			return findUniqueTag(item.getTags());
		}

		public List<T> find(Tags tags) {
			List<T> ret = Lists.newArrayList();
			for (String s : tags.find(key)) {
				ret.add(toT(s));
			}
			return ret;
		}

		protected abstract T toT(String s);

		public boolean isTag(Tag tag) {
			return key.equals(tag.getKey());
		}
	}

	@XmlTransient
	public static class KeyTagKey extends TagKey<PlatformLayerKey> {
		public KeyTagKey(String key) {
			super(key);
		}

		@Override
		protected PlatformLayerKey toT(String s) {
			return PlatformLayerKey.parse(s);
		}

		public Tag build(PlatformLayerKey t) {
			return new Tag(key, t.getUrl());
		}

	}

	@XmlTransient
	public static class StringTagKey extends TagKey<String> {
		public StringTagKey(String key) {
			super(key);
		}

		@Override
		protected String toT(String s) {
			return s;
		}

		public Tag build(String t) {
			return new Tag(key, t);
		}
	}

	@XmlTransient
	public static class EndpointTagKey extends TagKey<EndpointInfo> {
		public EndpointTagKey(String key) {
			super(key);
		}

		@Override
		protected EndpointInfo toT(String s) {
			return EndpointInfo.parseTagValue(s);
		}

		public Tag build(EndpointInfo t) {
			return new Tag(key, t.getTagValue());
		}
	}

	@XmlTransient
	public static class UuidTagKey extends TagKey<java.util.UUID> {
		public UuidTagKey(String key) {
			super(key);
		}

		@Override
		protected java.util.UUID toT(String s) {
			return java.util.UUID.fromString(s);
		}

		public Tag build(java.util.UUID t) {
			return new Tag(key, t.toString());
		}

	}

	@XmlTransient
	public static class AddressTagKey extends TagKey<InetAddress> {
		public AddressTagKey(String key) {
			super(key);
		}

		@Override
		protected InetAddress toT(String s) {
			return InetAddresses.forString(s);
		}

		public Tag build(InetAddress t) {
			return new Tag(key, t.getHostAddress());
		}
	}

	@XmlTransient
	public static class DateTagKey extends TagKey<Date> {
		public DateTagKey(String key) {
			super(key);
		}

		// Not static to avoid locking problems
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

		@Override
		protected Date toT(String s) {
			try {
				return dateFormat.parse(s);
			} catch (ParseException e) {
				throw new IllegalArgumentException("Cannot parse value as date: " + s, e);
			}
		}

		public Tag build(Date v) {
			String s = dateFormat.format(v);
			return new Tag(key, s);
		}
	}

	@XmlTransient
	public static class BooleanTagKey extends TagKey<Boolean> {
		public BooleanTagKey(String key) {
			super(key);
		}

		@Override
		protected Boolean toT(String s) {
			return Boolean.valueOf(s);
		}

		public Tag build(Boolean v) {
			String s = v.toString();
			return new Tag(key, s);
		}
	}

	public static final KeyTagKey PARENT = new KeyTagKey("parent");
	// public static final String RELATED = "linked";
	public static final String ASSIGNED = "assigned";
	public static final KeyTagKey ASSIGNED_TO = new KeyTagKey("assigned_to");

	public static final StringTagKey IMAGE_ID = new StringTagKey("imageid");
	public static final StringTagKey PUBLIC_KEY_SIG = new StringTagKey("publickeysig");

	public static final KeyTagKey INSTANCE_KEY = new KeyTagKey("instancekey");
	public static final AddressTagKey NETWORK_ADDRESS = new AddressTagKey("net.address");

	public static final String HOST_POLICY = "host.policy";
	public static final EndpointTagKey PUBLIC_ENDPOINT = new EndpointTagKey("public-endpoint");
	public static final String UNIQUE_ID = "uniqueid";

	public static final String IMAGE_OS_DISTRIBUTION = "org.openstack__1__os_distro";
	public static final String IMAGE_OS_VERSION = "org.openstack__1__os_version";

	public static final UuidTagKey UUID = new UuidTagKey("uuid");

	// public static final String IMAGE_TYPE = "platformlayer.org__type";

	public Tag() {
	}

	public Tag(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Tag other = (Tag) obj;
		if (key == null) {
			if (other.key != null) {
				return false;
			}
		} else if (!key.equals(other.key)) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Tag [" + key + "=" + value + "]";
	}

	public static Tag buildParentTag(PlatformLayerKey key) {
		return Tag.PARENT.build(key);
	}

	public static Tag buildTag(String tagKey, PlatformLayerKey itemKey) {
		return new Tag(tagKey, itemKey.getUrl());
	}

}
