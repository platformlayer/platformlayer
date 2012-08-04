package org.platformlayer.ops.images;

import org.platformlayer.EnumUtils;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;

public enum ImageFormat {
	Tar, DiskRaw, DiskQcow2;

	public static final String TAG_PLATFORMLAYER_IMAGE_FORMAT = "org.platformlayer__1__image_format";

	// public static final String TAG_OPENSTACK_GLANCE_IMAGE_FORMAT = "disk_format";

	public static ImageFormat fromTag(Tag tag) {
		if (!isImageFormatTag(tag)) {
			throw new IllegalArgumentException();
		}
		return EnumUtils.valueOfCaseInsensitive(ImageFormat.class, tag.getValue());
	}

	public static ImageFormat fromTags(Tags tags) {
		for (Tag tag : tags) {
			if (isImageFormatTag(tag)) {
				return fromTag(tag);
			}
		}
		return null;
	}

	public Tag toTag() {
		return new Tag(TAG_PLATFORMLAYER_IMAGE_FORMAT, name().toLowerCase());
	}

	public static boolean isImageFormatTag(Tag tag) {
		return tag.getKey().equals(TAG_PLATFORMLAYER_IMAGE_FORMAT);
	}
};