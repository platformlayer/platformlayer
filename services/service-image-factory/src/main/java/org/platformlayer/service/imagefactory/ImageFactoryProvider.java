package org.platformlayer.service.imagefactory;

import java.util.List;

import org.platformlayer.images.model.DiskImage;
import org.platformlayer.images.model.DiskImageRecipe;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.ServiceProviderBase;
import org.platformlayer.service.imagefactory.ops.DiskImageController;
import org.platformlayer.service.imagefactory.ops.DiskImageRecipeController;
import org.platformlayer.xaas.Service;
import org.platformlayer.xaas.services.ModelClass;

@Service("imagefactory")
public class ImageFactoryProvider extends ServiceProviderBase {
	@Override
	protected List<ModelClass<?>> buildModels() {
		List<ModelClass<?>> modelClasses = super.buildModels();

		modelClasses.add(ModelClass.publicModel(this, DiskImage.class));
		modelClasses.add(ModelClass.publicModel(this, DiskImageRecipe.class));

		return modelClasses;
	}

	@Override
	public Class<?> getControllerClass(Class<?> managedItemClass) throws OpsException {
		ensureInitialized();

		if (managedItemClass == DiskImage.class) {
			return DiskImageController.class;
		}

		if (managedItemClass == DiskImageRecipe.class) {
			return DiskImageRecipeController.class;
		}

		return super.getControllerClass(managedItemClass);
	}
}
