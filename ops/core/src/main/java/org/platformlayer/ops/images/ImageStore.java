package org.platformlayer.ops.images;

import java.io.File;
import java.util.List;

import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;
import org.platformlayer.ops.CloudImage;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

public interface ImageStore {
	CloudImage findImage(List<Tag> tags) throws OpsException;

	List<CloudImage> findImages(List<Tag> tags) throws OpsException;

	void updateImageTags(String imageId, Tags tags) throws OpsException;

	String uploadImage(OpsTarget imageHost, Tags tags, File imageFile, long rawImageFileSize) throws OpsException;

	void bringToMachine(String imageId, OpsTarget destination, File destinationPath) throws OpsException;
}
