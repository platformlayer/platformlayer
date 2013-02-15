package org.platformlayer.client.cli.commands;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.platformlayer.Format;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.UntypedItemXml;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.core.model.Link;
import org.platformlayer.core.model.Links;
import org.platformlayer.core.model.PlatformLayerKey;

public class DeleteLink extends PlatformLayerCommandRunnerBase {
	@Argument(index = 0, required = true, metaVar = "path")
	public ItemPath path;

	@Argument(index = 1, required = true, metaVar = "name")
	public String name;

	public DeleteLink() {
		super("delete", "link");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException {
		PlatformLayerClient client = getPlatformLayerClient();

		PlatformLayerKey resolved = path.resolve(getContext());

		UntypedItemXml item = (UntypedItemXml) client.getItemUntyped(resolved, Format.XML);

		Links links = item.getLinks();

		Link existing = links.findLink(name);
		List<Link> linkList = links.getLinks();
		if (existing != null) {
			linkList.remove(existing);

			item.setLinks(links);

			String xml = item.serialize();

			UntypedItemXml updated = (UntypedItemXml) client.putItem(resolved, xml, Format.XML);

			return updated.getLinks().getLinks();
		} else {
			return linkList;
		}
	}

	@Override
	public void formatRaw(Object o, PrintWriter writer) {
		Collection<Link> links = (Collection<Link>) o;
		for (Link link : links) {
			writer.println(link.getName() + "\t" + link.getTarget());
		}
	}
}
