package org.platformlayer.client.cli.commands;

import java.io.PrintWriter;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.kohsuke.args4j.Argument;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.UntypedItem;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.xml.XmlHelper;

import com.fathomdb.cli.commands.Ansi;

public class GetItem extends PlatformLayerCommandRunnerBase {
	@Argument(index = 0)
	public ItemPath path;

	public GetItem() {
		super("get", "item");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException {
		PlatformLayerClient client = getPlatformLayerClient();

		PlatformLayerKey key = path.resolve(getContext());
		return client.getItemUntyped(key);
	}

	@Override
	public void formatRaw(Object o, PrintWriter writer) {
		Ansi ansi = new Ansi(writer);

		UntypedItem item = (UntypedItem) o;

		Source src = new DOMSource(item.getRoot());
		String xml = XmlHelper.toXml(src, 4);

		ansi.println(xml);

		ansi.reset();
	}

}
