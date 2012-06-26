package org.platformlayer.client.cli.commands;

import java.io.File;
import java.net.Inet6Address;
import java.net.InetAddress;

import org.kohsuke.args4j.Argument;
import org.openstack.utils.Io;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.UntypedItem;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.xml.XmlHelper.ElementInfo;

import com.fathomdb.cli.CliException;
import com.fathomdb.cli.output.ClientAction;

public class SshItem extends PlatformLayerCommandRunnerBase {
	@Argument
	public ItemPath path;

	public SshItem() {
		super("ssh", "item");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException {
		PlatformLayerClient client = getPlatformLayerClient();

		PlatformLayerKey key = path.resolve(getContext());

		UntypedItem untypedItem = client.getItemUntyped(key);

		InetAddress sshAddress = findSshAddress(client, untypedItem);

		ClientAction action = null;
		if (sshAddress != null) {
			String user = "root";

			ProjectId project = key.getProject();
			if (project == null) {
				project = client.getProject();
			}
			if (project == null) {
				throw new CliException("Cannot determine project");
			}
			String projectKey = project.getKey();
			String serviceKey = "service-" + key.getServiceType().getKey();

			File sshKey = Io.resolve("~/.credentials/ssh/" + projectKey + "/" + serviceKey);

			// Hmmm... user? key?
			action = new ClientAction(ClientAction.ClientActionType.SSH, user + "@" + sshAddress.getHostAddress(),
					sshKey.getAbsolutePath());
		}

		return action;
	}

	private InetAddress findSshAddress(PlatformLayerClient client, UntypedItem untypedItem)
			throws PlatformLayerClientException {
		ElementInfo rootElementInfo = untypedItem.getRootElementInfo();

		if ("directInstance".equals(rootElementInfo.elementName)) {
			Tags itemTags = untypedItem.getTags();

			for (InetAddress address : Tag.NETWORK_ADDRESS.find(itemTags)) {
				if (address instanceof Inet6Address) {
					return address;
				}
			}

			// List<EndpointInfo> endpointList = EndpointInfo.getEndpoints(itemTags);
			//
			// Set<InetAddress> addresses = Sets.newHashSet();
			// for (EndpointInfo candidate : endpointList) {
			// InetAddress address = candidate.getAddress();
			// addresses.add(address);
			// }
			//
			// if (addresses.size() == 0) {
			// throw new CliException("Cannot find address");
			// }
			//
			// if (addresses.size() > 1) {
			// throw new CliException("Cannot choose between addresses: " + Joiner.on(",").join(addresses));
			// }
			//
			// InetAddress host = Iterables.getFirst(addresses, null);
			//
			// return host;
			// if (host == null) {
			// String addressTag = itemTags.findUnique(Tag.NETWORK_ADDRESS);
			// if (addressTag != null) {
			// int colonIndex = addressTag.indexOf(':');
			// if (colonIndex != -1) {
			// host = addressTag.substring(colonIndex);
			// }
			// }
			// }
		}

		for (UntypedItem child : client.listChildren(untypedItem.getPlatformLayerKey())) {
			InetAddress sshAddress = findSshAddress(client, child);
			if (sshAddress != null) {
				return sshAddress;
			}
		}

		return null;

	}
}
