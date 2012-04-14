package org.platformlayer.service.desktop.ops;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.packages.HasDiskImageRecipe;
import org.platformlayer.service.imagefactory.v1.DiskImageRecipe;
import org.platformlayer.service.imagefactory.v1.OperatingSystemRecipe;

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
