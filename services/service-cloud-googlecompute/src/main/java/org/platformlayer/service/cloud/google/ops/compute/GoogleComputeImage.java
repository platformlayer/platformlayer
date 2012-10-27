package org.platformlayer.service.cloud.google.ops.compute;

import org.platformlayer.ops.images.CloudImage;
import org.platformlayer.ops.images.ImageFormat;

import com.google.api.services.compute.model.Image;

public class GoogleComputeImage implements CloudImage {

	private final Image image;

	public GoogleComputeImage(Image image) {
		this.image = image;
	}

	@Override
	public String getId() {
		return image.getName();
	}

	@Override
	public ImageFormat getFormat() {
		throw new UnsupportedOperationException();
	}

}
