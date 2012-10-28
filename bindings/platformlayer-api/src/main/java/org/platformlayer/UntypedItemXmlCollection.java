package org.platformlayer;

import java.util.Iterator;
import java.util.List;

import org.platformlayer.common.UntypedItem;
import org.platformlayer.common.UntypedItemCollection;
import org.platformlayer.xml.XmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;

public class UntypedItemXmlCollection implements UntypedItemCollection, Iterable<UntypedItem> {
	final Element root;
	final List<UntypedItem> items = Lists.newArrayList();

	public UntypedItemXmlCollection(Element root) {
		this.root = root;

		findItems();
	}

	private void findItems() {
		Node itemsElement = XmlHelper.findUniqueChild(root, "items", false);
		if (itemsElement != null) {
			NodeList childNodes = itemsElement.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node child = childNodes.item(i);
				if (child instanceof Element) {
					Element childElement = (Element) child;
					// String namespaceURI = childElement.getNamespaceURI();
					String nodeName = childElement.getLocalName();
					if (nodeName.equals("item")) {
						UntypedItem untypedItem = new UntypedItemXml(childElement);

						items.add(untypedItem);
					}
				}
			}
		}
	}

	public static UntypedItemXmlCollection build(String xml) {
		Element documentElement;

		try {
			Document dom = XmlHelper.parseXmlDocument(xml, true);
			documentElement = dom.getDocumentElement();
		} catch (Exception e) {
			throw new IllegalArgumentException("Error parsing XML", e);
		}

		UntypedItemXmlCollection items = new UntypedItemXmlCollection(documentElement);
		// for (UntypedItem item : items) {
		// PlatformLayerKey platformLayerKey = new PlatformLayerKey(host, project, serviceType, itemType, id);
		// item.setPlatformLayerKey(platformLayerKey );
		// }
		return items;
	}

	@Override
	public Iterator<UntypedItem> iterator() {
		return items.iterator();
	}

	@Override
	public List<UntypedItem> getItems() {
		return items;
	}

}
