package org.platformlayer.ops;

import javax.inject.Inject;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.xaas.SingletonService;

import com.google.common.collect.Iterables;

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

        Class<? extends Object> modelClass = item.getClass();
        SingletonService singletonServiceAnnotation = modelClass.getAnnotation(SingletonService.class);
        if (singletonServiceAnnotation != null) {
            // Only one can be created per scope
            Iterable<?> items = platformLayer.listItems(modelClass);
            if (!Iterables.isEmpty(items)) {
                throw new OpsException("Cannot create multiple instances of: " + modelClass.getName());
            }
        }
    }

}
