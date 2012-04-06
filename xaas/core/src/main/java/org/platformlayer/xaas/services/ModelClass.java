package org.platformlayer.xaas.services;

import javax.xml.bind.JAXBException;

import org.platformlayer.CastUtils;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ids.ItemType;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.xml.JaxbHelper;
import org.platformlayer.xml.JsonHelper;

public class ModelClass<T extends ItemBase> {
    final Class<T> javaClass;
    final boolean systemObject;
    final ItemType itemType;
    final ServiceProvider serviceProvider;

    public ModelClass(ServiceProvider serviceProvider, Class<T> javaClass, ItemType itemType, boolean systemObject) {
        super();
        this.serviceProvider = serviceProvider;
        this.javaClass = javaClass;
        this.itemType = itemType;
        this.systemObject = systemObject;
    }

    public Class<T> getJavaClass() {
        return javaClass;
    }

    public boolean isSystemObject() {
        return systemObject;
    }

    public static <T extends ItemBase> ModelClass<T> privateModel(ServiceProvider serviceProvider, Class<T> clazz) {
        return build(serviceProvider, clazz, true);
    }

    public static <T extends ItemBase> ModelClass<T> publicModel(ServiceProvider serviceProvider, Class<T> clazz) {
        return build(serviceProvider, clazz, false);
    }

    public static <T extends ItemBase> ModelClass<T> build(ServiceProvider serviceProvider, Class<T> clazz, boolean management) {
        JaxbHelper jaxbHelper = JaxbHelper.get(clazz);
        ItemType itemType = new ItemType(jaxbHelper.getXmlElementName(clazz));
        return new ModelClass<T>(serviceProvider, clazz, itemType, management);
    }

    public JsonHelper<?> getJsonHelper() {
        return JsonHelper.build(javaClass);
    }

    public ItemType getItemType() {
        return itemType;
    }

    private JaxbHelper getJaxbHelper() {
        return JaxbHelper.get(getJavaClass());
    }

    public T deserializeXml(String modelData) throws JAXBException {
        return CastUtils.as(getJaxbHelper().unmarshal(modelData), getJavaClass());
    }

    public ServiceProvider getProvider() {
        return serviceProvider;
    }

    public ServiceType getServiceType() {
        return serviceProvider.getServiceType();
    }

    public String getPrimaryNamespace() {
        return getJaxbHelper().getPrimaryNamespace();
    }

    public String getXmlElementName() {
        return getJaxbHelper().getXmlElementName();
    }
}
