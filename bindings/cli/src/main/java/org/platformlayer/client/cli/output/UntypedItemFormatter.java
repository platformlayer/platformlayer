package org.platformlayer.client.cli.output;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.platformlayer.UntypedItemXml;
import org.platformlayer.common.UntypedItem;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.xml.XmlHelper;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fathomdb.cli.commands.Ansi;
import com.fathomdb.cli.formatter.SimpleFormatter;
import com.fathomdb.cli.output.OutputSink;
import com.google.common.collect.Maps;

@SuppressWarnings("rawtypes")
public class UntypedItemFormatter extends SimpleFormatter<UntypedItem> {
	public UntypedItemFormatter() {
		super(UntypedItem.class);
	}

	@Override
	public void visit(UntypedItem o, OutputSink sink) throws IOException {
		LinkedHashMap<String, Object> values = Maps.newLinkedHashMap();

		UntypedItemXml item = (UntypedItemXml) o;

		Element dataElement = item.getDataElement();

		// String xml = o.getModelData();
		//
		// Element documentElement;
		//
		// try {
		// Document dom = XmlHelper.parseXmlDocument(xml, false);
		// documentElement = dom.getDocumentElement();
		// } catch (Exception e) {
		// throw new IllegalArgumentException("Error parsing XML", e);
		// }

		values.put("key", item.getKey().getUrl());
		values.put("state", item.getState());

		NodeList childNodes = dataElement.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			values.put(node.getNodeName(), formatCell(node));
		}

		sink.outputRow(values);
	}

	private String formatCell(Node node) {
		switch (node.getNodeType()) {
		case Node.ATTRIBUTE_NODE:
			return node.getTextContent();

		case Node.ELEMENT_NODE:
			return formatElementAsCell((Element) node);

		case Node.TEXT_NODE:
			return node.getTextContent();

		default:
			throw new IllegalArgumentException("Unhandled node type: " + node);
		}
	}

	private String formatElementAsCell(Element node) {
		NodeList childNodes = node.getChildNodes();

		if (childNodes.getLength() == 0) {
			return "";
		}

		if (childNodes.getLength() == 1) {
			return formatCell(childNodes.item(0));
		}

		return XmlHelper.toXml(node);
	}

	public static void formatItem(UntypedItem o, Ansi ansi, boolean fullPath) {
		UntypedItemXml item = (UntypedItemXml) o;

		Ansi.Action action = null;

		switch (item.getState()) {
		case ACTIVE:
			action = Ansi.TEXT_COLOR_GREEN;
			break;

		case BUILD_ERROR:
			action = Ansi.TEXT_COLOR_RED;
			break;

		case DELETED:
			action = Ansi.TEXT_COLOR_MAGENTA;
			break;

		default:
			action = Ansi.TEXT_COLOR_BLUE;
			break;
		}

		Ansi.Action undo = null;

		if (action != null) {
			undo = ansi.doAction(action);
		}

		try {
			PlatformLayerKey plk = item.getKey();

			if (fullPath) {
				ansi.println(plk.getUrl());
			} else {
				ansi.println(plk.getItemId().getKey());
			}
		} finally {
			if (undo != null) {
				ansi.doAction(undo);
			}
		}

	}

}
