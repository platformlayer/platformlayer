package org.platformlayer.client.cli.commands;

import java.net.InetAddress;
import java.util.List;
import java.util.Set;

import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.UntypedItem;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;
import org.platformlayer.xml.XmlHelper.ElementInfo;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class SshAddressFinder {
	public final List<InetAddress> found = Lists.newArrayList();

	final PlatformLayerClient client;

	public SshAddressFinder(PlatformLayerClient client) {
		super();
		this.client = client;
	}

	public void visit(UntypedItem untypedItem) throws PlatformLayerClientException {
		ElementInfo rootElementInfo = untypedItem.getRootElementInfo();

		boolean consider = true;

		switch (untypedItem.getState()) {
		case DELETED:
		case DELETE_REQUESTED:
			consider = false;
			break;
		}

		Set<String> instanceTypes = Sets.newHashSet();
		instanceTypes.add("directInstance");
		instanceTypes.add("googleCloudInstance");

		if (!instanceTypes.contains(rootElementInfo.elementName)) {
			consider = false;
		}

		if (consider) {
			Tags itemTags = untypedItem.getTags();

			for (InetAddress address : Tag.NETWORK_ADDRESS.find(itemTags)) {
				found.add(address);
			}
		}

		for (UntypedItem child : client.listChildren(untypedItem.getPlatformLayerKey())) {
			visit(child);
		}

	}
}
