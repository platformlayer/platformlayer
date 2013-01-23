package org.platformlayer.ops;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;

import com.google.common.base.Strings;

public class UniqueTag {
	public static Tag build(ItemBase item) {
		String s = null;
		s = append(s, item);
		return Tag.build(Tag.UNIQUE_ID, s);
	}

	public static Tag build(ItemBase item, ItemBase item2, String key) {
		String s = null;
		s = append(s, item);
		s = append(s, item2);
		s = join(s, key);
		return Tag.build(Tag.UNIQUE_ID, s);
	}

	public static Tag build(ItemBase item, ItemBase item2) {
		String s = null;
		s = append(s, item);
		s = append(s, item2);
		return Tag.build(Tag.UNIQUE_ID, s);
	}

	public static Tag build(ItemBase item, String key) {
		String s = null;
		s = append(s, item);
		s = join(s, key);
		return Tag.build(Tag.UNIQUE_ID, s);
	}

	public static Tag build(PlatformLayerKey item, String key) {
		String s = null;
		s = append(s, item);
		s = join(s, key);
		return Tag.build(Tag.UNIQUE_ID, s);
	}

	private static String join(String s, String suffix) {
		if (!Strings.isNullOrEmpty(s)) {
			return s + "::" + suffix;
		}
		return suffix;
	}

	private static String append(String s, ItemBase item) {
		return append(s, item.getKey());
	}

	private static String append(String s, PlatformLayerKey itemKey) {
		if (itemKey.getHost() != null) {
			throw new UnsupportedOperationException();
		}
		String add = itemKey.getServiceType().getKey() + "/" + itemKey.getItemType().getKey() + "/"
				+ itemKey.getItemId().getKey();
		return join(s, add);
	}
}
