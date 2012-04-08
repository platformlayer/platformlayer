package org.platformlayer.ops;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;

import com.google.common.base.Strings;

public class UniqueTag {
    public static Tag build(ItemBase item) {
        String s = null;
        s = append(s, item);
        return new Tag(Tag.UNIQUE_ID, s);
    }

    public static Tag build(ItemBase item, ItemBase item2) {
        String s = null;
        s = append(s, item);
        s = append(s, item2);
        return new Tag(Tag.UNIQUE_ID, s);
    }

    public static Tag build(ItemBase item, String key) {
        String s = null;
        s = append(s, item);
        s = join(s, key);
        return new Tag(Tag.UNIQUE_ID, s);
    }

    private static String join(String s, String suffix) {
        if (!Strings.isNullOrEmpty(s))
            return s + "::" + suffix;
        return suffix;
    }

    private static String append(String s, ItemBase item) {
        PlatformLayerKey key = OpsSystem.toKey(item);
        if (key.getHost() != null) {
            throw new UnsupportedOperationException();
        }
        String add = key.getServiceType().getKey() + "/" + key.getItemType().getKey() + "/" + key.getItemId().getKey();
        return join(s, add);
    }
}
