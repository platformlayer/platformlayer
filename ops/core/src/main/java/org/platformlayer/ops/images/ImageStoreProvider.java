package org.platformlayer.ops.images;

import org.platformlayer.ops.OpsException;

public interface ImageStoreProvider {

	ImageStore getImageStore() throws OpsException;

}
