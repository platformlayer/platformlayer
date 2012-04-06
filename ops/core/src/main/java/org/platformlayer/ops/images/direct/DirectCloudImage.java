package org.platformlayer.ops.images.direct;

import org.platformlayer.ops.CloudImage;

public class DirectCloudImage implements CloudImage {
    final DirectImageStore store;
    final String imageId;

    public DirectCloudImage(DirectImageStore store, String imageId) {
        this.store = store;
        this.imageId = imageId;
    }

    @Override
    public String getId() {
        return imageId;
    }

}
