package org.platformlayer.client.cli.commands;

import org.kohsuke.args4j.Argument;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.UntypedItemXml;
import org.platformlayer.client.cli.model.ItemPath;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.fathomdb.cli.CliException;

public class AddConfig extends ItemMutatorCommand {

	@Argument(index = 0, required = true, metaVar = "path")
	public ItemPath path;

	@Argument(index = 1, required = true, metaVar = "property")
	public String property;

	@Argument(index = 2, required = true, metaVar = "key")
	public String key;

	@Argument(index = 3, required = true, metaVar = "value")
	public String value;

	public AddConfig() {
		super("add", "config");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException {
		UntypedItemXml item = runCommand(path);
		return item;
	}

	@Override
	protected void changeItem(UntypedItemXml item) {
		Element element = getElement(item, property);
		if (element == null) {
			throw new CliException("Cannot find element: " + property);
		}

		String namespaceURI = element.getNamespaceURI();
		String localName = element.getLocalName();

		Node parentNode = element.getParentNode();
		String parentNamespaceUri = parentNode.getNamespaceURI();
		String parentTag = parentNode.getLocalName();

		String pathKey = parentNamespaceUri + ":" + parentTag + ":" + namespaceURI + ":" + localName;

		if ("http://platformlayer.org/service/platformlayer/v1.0:platformLayerService:http://platformlayer.org/service/platformlayer/v1.0:config"
				.equals(pathKey)) {
			Element newNode = element.getOwnerDocument().createElementNS(NAMESPACE_URI_CORE, "property");
			Element keyNode = element.getOwnerDocument().createElementNS(NAMESPACE_URI_CORE, "key");
			keyNode.setTextContent(key);
			Element valueNode = element.getOwnerDocument().createElementNS(NAMESPACE_URI_CORE, "value");
			valueNode.setTextContent(value);
			newNode.appendChild(keyNode);
			newNode.appendChild(valueNode);

			element.appendChild(newNode);
		} else {
			throw new UnsupportedOperationException();
		}
	}

}
