package org.platformlayer.client.cli.commands;

import java.io.IOException;
import java.io.InputStream;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.UntypedItemXml;
import org.platformlayer.client.cli.model.ItemPath;
import org.w3c.dom.Element;

import com.fathomdb.cli.CliException;
import com.fathomdb.io.NoCloseInputStream;
import com.fathomdb.utils.Base64;
import com.google.common.io.ByteStreams;

public class SetProperty extends ItemMutatorCommand {
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
				value = Base64.encode(data);
			} else {
				value = new String(data);
			}
		} else {
			if (value == null) {
				throw new CliException("Value is required (if not using -stdin)");
			}
		}

		return runCommand(path);
	}

	@Override
	protected void changeItem(UntypedItemXml item) {
		Element element = getElement(item, key);

		if (element == null) {
			throw new CliException("Cannot find element: " + key);
		}

		element.setTextContent(value);
	}

}
