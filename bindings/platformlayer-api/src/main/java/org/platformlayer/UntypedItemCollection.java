package org.platformlayer;

import java.util.Iterator;
import java.util.List;

import org.platformlayer.xml.XmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;

public class UntypedItemCollection implements Iterable<UntypedItem> {
	final Element root;
	final List<UntypedItem> items;

	public UntypedItemCollection(Element root) {
		this.root = root;

		this.items = findItems();
	}

	private List<UntypedItem> findItems() {
		List<UntypedItem> items = Lists.newArrayList();

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
						UntypedItem untypedItem = new UntypedItem(childElement);

						items.add(untypedItem);
					}
				}
			}
		}
		return items;
	}

	public static UntypedItemCollection build(String xml) {
		Element documentElement;

		try {
			Document dom = XmlHelper.parseXmlDocument(xml, true);
			documentElement = dom.getDocumentElement();
		} catch (Exception e) {
			throw new IllegalArgumentException("Error parsing XML", e);
		}

		UntypedItemCollection items = new UntypedItemCollection(documentElement);
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

}
