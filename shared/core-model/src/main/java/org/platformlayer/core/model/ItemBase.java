package org.platformlayer.core.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.platformlayer.common.IsItem;
import org.platformlayer.ids.ManagedItemId;

@XmlAccessorType(XmlAccessType.FIELD)
// It's not really a root element (?), but this helps jersey
@XmlRootElement
public class ItemBase implements IsItem {
	public PlatformLayerKey key;

	public long version;

	public ManagedItemState state;

	public Tags tags;

	public Links links;

	@XmlTransient
	public SecretInfo secret;

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	@Override
	public Tags getTags() {
		if (tags == null) {
			tags = Tags.build();
		}
		return tags;
	}

	@Override
	public ManagedItemState getState() {
		return state;
	}

	@Deprecated
	public static ItemBase force(Object item) {
		return (ItemBase) item;
	}

	@Override
	public PlatformLayerKey getKey() {
		return key;
	}

	public void setKey(PlatformLayerKey key) {
		this.key = key;
	}

	public String getId() {
		ManagedItemId itemId = null;
		if (key != null) {
			itemId = key.getItemId();
		}
		if (itemId != null) {
			return itemId.getKey();
		}
		return null;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [key=" + key + "]";
	}

	// @Override
	// public String findFirst(String key) {
	// return getTags().findFirst(key);
	// }
	//
	// @Override
	// public String findUnique(String key) {
	// return getTags().findUnique(key);
	// }
	//
	// @Override
	// public List<String> findAll(String key) {
	// return getTags().findAll(key);
	// }
	//
	// @Override
	// public void addTag(IsTag tag) {
	// return getTags().addTag(tag);
	// }

}
