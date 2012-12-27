package org.platformlayer.client.cli.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.platformlayer.Format;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.UntypedItemXml;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.common.UntypedItem;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.crypto.CryptoUtils;
import org.platformlayer.xml.XmlHelper;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.fathomdb.cli.CliException;
import com.fathomdb.io.NoCloseInputStream;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;

public class SetProperty extends PlatformLayerCommandRunnerBase {
	@Option(name = "-stdin", usage = "Read value from stdin")
	public boolean stdin;

	@Option(name = "-format", usage = "Format of data in stdin")
	public String format;

	@Argument(index = 0, required = true, metaVar = "path")
	public ItemPath path;

	@Argument(index = 1, required = true, metaVar = "key")
	public String key;
	@Argument(index = 2, required = false, metaVar = "value")
	public String value;

	public SetProperty() {
		super("set", "property");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException, IOException {
		PlatformLayerClient client = getPlatformLayerClient();

		if (stdin) {
			if (value != null) {
				throw new CliException("You cannot specify a value when using -stdin");
			}

			InputStream stream = new NoCloseInputStream(System.in);
			byte[] data = ByteStreams.toByteArray(stream);

			if ("base64".equals(format)) {
				value = CryptoUtils.toBase64(data);
			} else {
				value = new String(data);
			}
		} else if (value == null) {
			throw new CliException("Value is required (if not using -stdin)");
		}

		PlatformLayerKey resolved = path.resolve(getContext());

		UntypedItemXml item = (UntypedItemXml) client.getItemUntyped(resolved);

		Element element = item.getRoot();
		List<String> tokens = Lists.newArrayList(Splitter.on(".").split(key));
		for (int i = 0; i < tokens.size(); i++) {
			String token = tokens.get(i);
			Node child = XmlHelper.getChildElement(element, token);
			if (child == null) {
				if (i == tokens.size() - 1) {
					child = element.getOwnerDocument().createElement(token);
					element.appendChild(child);
				} else {
					throw new CliException("Cannot find element: " + token);
				}
			}
			element = (Element) child;
		}

		element.setTextContent(value);

		String xml = item.serialize();
		// System.out.println(xml);
		UntypedItem updated = client.putItem(resolved, xml, Format.XML);

		return updated;
	}

	@Override
	public void formatRaw(Object o, PrintWriter writer) {
		UntypedItem item = (UntypedItem) o;
		writer.println(item.getKey());
	}

}
