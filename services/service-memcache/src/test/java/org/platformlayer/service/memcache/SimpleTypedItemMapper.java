package org.platformlayer.service.memcache;

import java.util.List;

import org.platformlayer.TypedItemMapper;
import org.platformlayer.ops.OpsException;
import org.platformlayer.service.memcache.model.MemcacheServer;
import org.platformlayer.xml.XmlHelper;
import org.platformlayer.xml.XmlHelper.ElementInfo;

import com.google.common.collect.Lists;

public class SimpleTypedItemMapper extends TypedItemMapper {

    final List<Class<?>> modelClasses = Lists.newArrayList();

    public SimpleTypedItemMapper() {
    }

    @Override
    protected <T> Class<T> mapToJavaClass(ElementInfo elementInfo) throws OpsException {
        for (Class<?> modelClass : modelClasses) {
            ElementInfo classElementInfo = XmlHelper.getXmlElementInfo(modelClass);
            if (elementInfo.equals(classElementInfo)) {
                return (Class<T>) modelClass;
            }
        }

        throw new OpsException("Unknown item type: " + elementInfo);
    }

    public void addClass(Class<?> modelClass) {
        modelClasses.add(modelClass);
    }

}
