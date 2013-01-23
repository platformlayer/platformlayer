package org.platformlayer.core.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.platformlayer.common.AddressTagKey;
import org.platformlayer.common.EndpointTagKey;
import org.platformlayer.common.KeyTagKey;
import org.platformlayer.common.StringTagKey;
import org.platformlayer.common.UuidTagKey;

@XmlAccessorType(XmlAccessType.FIELD)
public class Tag /* implements IsTag */{
	public String key;
	public String value;

	public static final String OWNER_ID = "owner";

	// public static final String PLATFORM_LAYER_ID = "platformlayerid";

	public static final KeyTagKey PARENT = new KeyTagKey("parent");
	// public static final String RELATED = "linked";
	public static final String ASSIGNED = "assigned";
	public static final KeyTagKey ASSIGNED_TO = new KeyTagKey("assigned_to");

	public static final StringTagKey POOL_ASSIGNMENT = new StringTagKey("poolmap");

	public static final StringTagKey IMAGE_ID = new StringTagKey("imageid");
	public static final StringTagKey PUBLIC_KEY_SIG = new StringTagKey("publickeysig");

	public static final KeyTagKey INSTANCE_KEY = new KeyTagKey("instancekey");
	public static final AddressTagKey NETWORK_ADDRESS = new AddressTagKey("net.address");

	public static final StringTagKey HOST_POLICY = new StringTagKey("host.policy");
	public static final EndpointTagKey PUBLIC_ENDPOINT = new EndpointTagKey("public-endpoint");
	public static final String UNIQUE_ID = "uniqueid";

	public static final String IMAGE_OS_DISTRIBUTION = "org.openstack__1__os_distro";
	public static final String IMAGE_OS_VERSION = "org.openstack__1__os_version";

	public static final UuidTagKey UUID = new UuidTagKey("uuid");

	public static final StringTagKey SSH_KEY = new StringTagKey("sshkey");

	// public static final String IMAGE_TYPE = "platformlayer.org__type";

	public Tag() {
	}

	public static Tag build(String key, String value) {
		Tag tag = new Tag();
		tag.key = key;
		tag.value = value;
		return tag;
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
		return Tag.build(tagKey, itemKey.getUrl());
	}

}
