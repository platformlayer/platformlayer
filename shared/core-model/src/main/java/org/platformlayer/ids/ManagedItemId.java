package org.platformlayer.ids;

import org.platformlayer.model.StringWrapper;

public class ManagedItemId extends StringWrapper {

    public ManagedItemId(String key) {
        super(key);
        if (key == null || key.isEmpty())
            throw new IllegalArgumentException();
    }

}
