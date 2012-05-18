package org.platformlayer;

import javax.xml.bind.JAXBException;

import org.openstack.utils.Casts;
import org.platformlayer.ops.OpsException;
import org.platformlayer.xml.JaxbHelper;
import org.platformlayer.xml.XmlHelper.ElementInfo;
import org.w3c.dom.Element;

public abstract class TypedItemMapper {

	public <T> T promoteToTyped(UntypedItem untypedItem) throws OpsException {
		ElementInfo elementInfo = untypedItem.getRootElementInfo();

		Class<T> javaClass = mapToJavaClass(elementInfo);

		return promoteToTyped(untypedItem, javaClass);
	}

	protected abstract <T> Class<T> mapToJavaClass(ElementInfo elementInfo) throws OpsException;

	public <T> T promoteToTyped(UntypedItem untypedItem, Class<T> itemClass) throws OpsException {
		JaxbHelper jaxbHelper = JaxbHelper.get(itemClass);
		T typedItem;
		try {
			Element element = untypedItem.getDataElement();
			T object = jaxbHelper.unmarshal(element, itemClass);

			if (!(object.getClass().isAssignableFrom(itemClass))) {
				System.out.println("XML = " + untypedItem.serialize());
			}

			typedItem = Casts.checkedCast(object, itemClass);
		} catch (JAXBException e) {
			throw new OpsException("Error deserializing item", e);
		}

		return typedItem;
	}
}
