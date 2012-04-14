package org.platformlayer.ops.packages;

import org.platformlayer.service.imagefactory.v1.DiskImageRecipe;

public interface HasDiskImageRecipe {
	public void addTo(DiskImageRecipe recipe);
}
