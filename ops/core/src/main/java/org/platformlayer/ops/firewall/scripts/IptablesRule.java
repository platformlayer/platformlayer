package org.platformlayer.ops.firewall.scripts;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.firewall.Transport;

public interface IptablesRule {
	Transport getTransport();

	Command buildIptablesAddCommand();

	Command buildIptablesDeleteCommand();
}
