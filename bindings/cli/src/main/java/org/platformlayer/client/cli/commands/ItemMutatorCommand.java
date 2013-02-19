package org.platformlayer.client.cli.commands;

import java.util.List;

import org.platformlayer.Format;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.UntypedItemXml;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.xml.XmlHelper;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public abstract class ItemMutatorCommand extends PlatformLayerCommandRunnerBase {

	public ItemMutatorCommand(String verb, String noun) {
		super(verb, noun);
	}

	protected UntypedItemXml runCommand(ItemPath path) throws PlatformLayerClientException {
		PlatformLayerClient client = getPlatformLayerClient();

		PlatformLayerKey resolved = path.resolve(getContext());

		UntypedItemXml item = (UntypedItemXml) client.getItemUntyped(resolved, Format.XML);

		changeItem(item);

		String xml = item.serialize();

		UntypedItemXml updated = (UntypedItemXml) client.putItem(resolved, xml, Format.XML);

		return updated;
	}

	protected abstract void changeItem(UntypedItemXml item);

	protected Element getElement(UntypedItemXml item, String propertyPath) {
		Element element = item.getRoot();
		List<String> tokens = Lists.newArrayList(Splitter.on(".").split(propertyPath));
		for (int i = 0; i < tokens.size(); i++) {
			String token = tokens.get(i);
			Node child = XmlHelper.getChildElement(element, token);
			if (child == null) {
				if (i == tokens.size() - 1) {
					child = element.getOwnerDocument().createElement(token);
					element.appendChild(child);
				} else {
					return null;
				}
			}
			element = (Element) child;
		}
		return element;
	}
}
