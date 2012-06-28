package org.platformlayer.service.cloud.google.ops.openstack;

import org.openstack.model.image.Image;
import org.platformlayer.ops.images.CloudImage;
import org.platformlayer.ops.images.ImageFormat;

public class GlanceImage implements CloudImage {

	private final Image image;

	public GlanceImage(Image image) {
		this.image = image;
	}

	@Override
	public String getId() {
		return image.getId();
	}

	@Override
	public ImageFormat getFormat() {
		throw new UnsupportedOperationException();
	}

}
