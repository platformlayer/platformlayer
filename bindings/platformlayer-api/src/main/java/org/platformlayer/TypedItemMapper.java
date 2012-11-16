package org.platformlayer;

import javax.xml.bind.JAXBException;

import org.platformlayer.common.UntypedItem;
import org.platformlayer.ops.OpsException;
import org.platformlayer.xml.JaxbHelper;
import org.platformlayer.xml.XmlHelper.ElementInfo;
import org.w3c.dom.Element;

import com.fathomdb.Casts;
import com.google.common.base.Objects;

public abstract class TypedItemMapper {

	public <T> T promoteToTyped(UntypedItem untypedItem) throws OpsException {
		ElementInfo elementInfo = ((UntypedItemXml) untypedItem).getRootElementInfo();

		Class<T> javaClass = mapToJavaClass(elementInfo);

		return promoteToTyped(untypedItem, javaClass);
	}

	protected abstract <T> Class<T> mapToJavaClass(ElementInfo elementInfo) throws OpsException;

	public <T> T promoteToTyped(UntypedItem untypedItem, Class<T> itemClass) throws OpsException {
		JaxbHelper jaxbHelper = JaxbHelper.get(itemClass);
		T typedItem;
		try {
			Element element = ((UntypedItemXml) untypedItem).getDataElement();

			String xmlElementName = jaxbHelper.getXmlElementName();
			String nodeName = element.getLocalName();
			if (!Objects.equal(xmlElementName, nodeName)) {
				String type = element.getAttribute("xsi:type");

				if (type != null && type.endsWith(":" + xmlElementName)) {
					// OK
				} else {
					throw new OpsException("Incorrect element type: " + xmlElementName + " vs " + nodeName);
				}
			}

			T object = jaxbHelper.unmarshal(element, itemClass);

			if (!(object.getClass().isAssignableFrom(itemClass))) {
				System.out.println("XML = " + ((UntypedItemXml) untypedItem).serialize());
			}

			typedItem = Casts.checkedCast(object, itemClass);
		} catch (JAXBException e) {
			throw new OpsException("Error deserializing item", e);
		}

		return typedItem;
	}
}
