package org.platformlayer.client.cli.output;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import org.platformlayer.UntypedItemXml;
import org.platformlayer.client.cli.PlatformLayerCliContext;
import org.platformlayer.common.UntypedItem;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;
import org.platformlayer.xml.XmlHelper;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fathomdb.cli.CliContext;
import com.fathomdb.cli.commands.Ansi;
import com.fathomdb.cli.formatter.SimpleFormatter;
import com.fathomdb.cli.output.OutputSink;
import com.google.common.collect.Maps;

public class UntypedItemFormatter extends SimpleFormatter<UntypedItem> {
	public UntypedItemFormatter() {
		super(UntypedItem.class);
	}

	@Override
	public void visit(CliContext contextGeneric, UntypedItem o, OutputSink sink) throws IOException {
		PlatformLayerCliContext context = (PlatformLayerCliContext) contextGeneric;

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

		values.put("key", Utils.formatUrl(context, item.getKey()));
		values.put("state", item.getState());

		Tags tags = item.getTags();
		values.put("tags", tagsToString(context, tags));

		NodeList childNodes = dataElement.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			String nodeName = node.getNodeName();
			String localName = node.getLocalName();
			String namespace = node.getNamespaceURI();

			if (namespace.equals("http://platformlayer.org/core/v1.0")) {
				if (localName.equals("tags")) {
					continue;
				}

				if (localName.equals("key")) {
					continue;
				}

				if (localName.equals("version")) {
					continue;
				}

				if (localName.equals("state")) {
					continue;
				}
			}

			String text = formatCell(node);
			text = Utils.reformatText(context, text);
			values.put(nodeName, text);
		}

		sink.outputRow(values);
	}

	private String tagsToString(PlatformLayerCliContext context, Tags tags) {
		if (tags == null) {
			return "";
		}

		List<Tag> tagList = tags.getTags();
		if (tagList == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for (Tag tag : tagList) {
			if (sb.length() != 0) {
				sb.append(", ");
			}
			String value = tag.getValue();
			value = Utils.reformatText(context, value);
			sb.append(tag.getKey() + "=" + value);
		}

		return sb.toString();
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
				ansi.print(plk.getUrl());
			} else {
				ansi.print(plk.getItemId().getKey());
			}
		} finally {
			if (undo != null) {
				ansi.doAction(undo);
			}
		}

		ansi.println();

	}

}
