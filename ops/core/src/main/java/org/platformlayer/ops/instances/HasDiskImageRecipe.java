package org.platformlayer.ops.instances;

import org.platformlayer.images.model.DiskImageRecipe;

public interface HasDiskImageRecipe {
	public void addTo(DiskImageRecipe recipe);
}
