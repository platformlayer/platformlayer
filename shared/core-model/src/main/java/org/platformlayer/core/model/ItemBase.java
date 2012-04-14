package org.platformlayer.core.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.ids.ManagedItemId;

@XmlAccessorType(XmlAccessType.FIELD)
// It's not really a root element (?), but this helps jersey
@XmlRootElement
public class ItemBase {
	public PlatformLayerKey key;

	public long version;

	public ManagedItemState state;
	public Tags tags;

	public SecretInfo secret;

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public Tags getTags() {
		if (tags == null) {
			tags = new Tags();
		}
		return tags;
	}

	public ManagedItemState getState() {
		return state;
	}

	@Deprecated
	public static ItemBase force(Object item) {
		return (ItemBase) item;
	}

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

}
