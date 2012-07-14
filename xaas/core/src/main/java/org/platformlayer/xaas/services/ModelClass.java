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
	final ItemType itemType;
	final ServiceProvider serviceProvider;

	public ModelClass(ServiceProvider serviceProvider, Class<T> javaClass, ItemType itemType) {
		this.serviceProvider = serviceProvider;
		this.javaClass = javaClass;
		this.itemType = itemType;
	}

	public Class<T> getJavaClass() {
		return javaClass;
	}

	public static <T extends ItemBase> ModelClass<T> publicModel(ServiceProvider serviceProvider, Class<T> clazz) {
		return build(serviceProvider, clazz);
	}

	public static <T extends ItemBase> ModelClass<T> build(ServiceProvider serviceProvider, Class<T> clazz) {
		JaxbHelper jaxbHelper = JaxbHelper.get(clazz);
		ItemType itemType = new ItemType(JaxbHelper.getXmlElementName(clazz));
		return new ModelClass<T>(serviceProvider, clazz, itemType);
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

	@Override
	public String toString() {
		return "ModelClass [javaClass=" + javaClass + "]";
	}
}
