package org.platformlayer.ops.packages;

import org.platformlayer.images.model.DiskImageRecipe;
import org.platformlayer.images.model.OperatingSystemRecipe;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.instances.HasDiskImageRecipe;

public class RecipeOperatingSystem implements HasDiskImageRecipe {

	public OperatingSystemRecipe operatingSystem;

	@Handler
	public void handler() {
	}

	@Override
	public void addTo(DiskImageRecipe recipe) {
		recipe.setOperatingSystem(operatingSystem);
	}

}
