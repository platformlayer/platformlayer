package org.platformlayer.service.cloud.openstack.ops.openstack;

import org.openstack.model.image.Image;
import org.platformlayer.ops.CloudImage;

public class GlanceImage implements CloudImage {

	private final Image image;

	public GlanceImage(Image image) {
		this.image = image;
	}

	@Override
	public String getId() {
		return image.getId();
	}

}
