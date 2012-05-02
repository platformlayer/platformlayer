package org.platformlayer.ops.images.direct;

import org.platformlayer.core.model.Tags;
import org.platformlayer.ops.images.CloudImage;
import org.platformlayer.ops.images.ImageFormat;

public class DirectCloudImage implements CloudImage {
	final DirectImageStore store;
	final String imageId;
	final Tags tags;

	public DirectCloudImage(DirectImageStore store, String imageId, Tags tags) {
		this.store = store;
		this.imageId = imageId;
		this.tags = tags;
	}

	@Override
	public String getId() {
		return imageId;
	}

	@Override
	public ImageFormat getFormat() {
		return ImageFormat.fromTags(tags);
	}

}
