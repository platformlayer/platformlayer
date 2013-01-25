package org.platformlayer.client.cli.commands;

import java.io.File;
import java.net.Inet6Address;
import java.net.InetAddress;

import org.kohsuke.args4j.Argument;
import org.platformlayer.Format;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.common.UntypedItem;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ids.ProjectId;

import com.fathomdb.cli.CliException;
import com.fathomdb.cli.output.ClientAction;
import com.fathomdb.io.IoUtils;

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

		UntypedItem untypedItem = client.getItemUntyped(key, Format.XML);

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

			File sshKey = IoUtils.resolve("~/.credentials/ssh/" + projectKey + "/" + serviceKey);

			// Hmmm... user? key?
			action = new ClientAction(ClientAction.ClientActionType.SSH, user + "@" + sshAddress.getHostAddress(),
					sshKey.getAbsolutePath());
		}

		return action;
	}

	private InetAddress findSshAddress(PlatformLayerClient client, UntypedItem untypedItem)
			throws PlatformLayerClientException {
		SshAddressFinder finder = new SshAddressFinder(client);
		finder.visit(untypedItem);

		// IPV6 addresses aren't behind NAT, and so we prefer them
		for (InetAddress address : finder.found) {
			if (address instanceof Inet6Address) {
				return address;
			}
		}

		// Fallback to whatever we can find..
		for (InetAddress address : finder.found) {
			return address;
		}

		return null;

	}
}
