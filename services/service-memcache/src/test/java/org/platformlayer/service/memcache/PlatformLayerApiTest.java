package org.platformlayer.service.memcache;

import org.testng.annotations.Test;

@Test
public class PlatformLayerApiTest extends AbstractPlatformLayerTest {
    SimpleTypedItemMapper typedItemMapper;

    protected void reset() {
        typedItemMapper = null;

        super.reset();
    }

    @Override
    protected SimpleTypedItemMapper getTypedItemMapper() {
        if (typedItemMapper == null) {
            typedItemMapper = new SimpleTypedItemMapper();
        }
        return typedItemMapper;
    }

}
