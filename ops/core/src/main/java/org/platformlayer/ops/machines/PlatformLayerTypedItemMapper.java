package org.platformlayer.ops.machines;

import javax.inject.Inject;

import org.platformlayer.TypedItemMapper;
import org.platformlayer.ops.OpsException;
import org.platformlayer.xaas.services.ModelClass;
import org.platformlayer.xml.XmlHelper.ElementInfo;

public class PlatformLayerTypedItemMapper extends TypedItemMapper {
    final ServiceProviderHelpers serviceProviderHelpers;

    @Inject
    public PlatformLayerTypedItemMapper(ServiceProviderHelpers serviceProviderHelpers) {
        this.serviceProviderHelpers = serviceProviderHelpers;
    }

    @Override
    protected <T> Class<T> mapToJavaClass(ElementInfo elementInfo) throws OpsException {
        ModelClass<?> modelClass = serviceProviderHelpers.getModelClass(elementInfo.namespace, elementInfo.elementName);
        if (modelClass == null) {
            throw new OpsException("Unknown item type: " + elementInfo);
        }

        Class<T> javaClass = (Class<T>) modelClass.getJavaClass();
        return javaClass;
    }
}