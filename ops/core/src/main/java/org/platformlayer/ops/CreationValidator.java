package org.platformlayer.ops;

import javax.inject.Inject;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.xaas.SingletonService;

public class CreationValidator {
	@Inject
	PlatformLayerHelpers platformLayer;

	public void validateCreateItem(ItemBase item) throws OpsException {
		// Object model;
		// try {
		// model = managed.getModel(); // Throws if not valid XML
		// } catch (Exception e) {
		// throw new OpsException("Invalid model", e);
		// }

		Class<? extends ItemBase> modelClass = item.getClass();
		SingletonService singletonServiceAnnotation = modelClass.getAnnotation(SingletonService.class);
		if (singletonServiceAnnotation != null) {
			// Only one can be created per scope
			Iterable<? extends ItemBase> items = platformLayer.listItems(modelClass);

			int aliveCount = 0;
			for (ItemBase peer : items) {
				switch (peer.getState()) {
				case ACTIVE:
				case CREATION_REQUESTED:
				case BUILD:
				case BUILD_ERROR:
					aliveCount++;
					break;

				case DELETE_REQUESTED:
				case DELETED:
					break;

				default:
					throw new IllegalStateException();
				}
			}
			if (aliveCount != 0) {
				throw new OpsException("Cannot create multiple instances of: " + modelClass.getName());
			}
		}
	}

}
