package org.platformlayer.client.cli.commands;

import org.codehaus.jettison.json.JSONException;
import org.kohsuke.args4j.Argument;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.client.cli.autocomplete.AutoCompleteServiceType;

import com.fathomdb.cli.autocomplete.AutoCompletor;
import com.fathomdb.cli.autocomplete.SimpleAutoCompleter;

public class GetSshKey extends PlatformLayerCommandRunnerBase {
	@Argument(index = 0, required = true)
	public String serviceType;

	public GetSshKey() {
		super("get", "sshkey");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException, JSONException {
		PlatformLayerClient client = getPlatformLayerClient();

		String sshKey = client.getSshPublicKey(serviceType);

		return sshKey;
	}

	@Override
	public AutoCompletor getAutoCompleter() {
		return new SimpleAutoCompleter(new AutoCompleteServiceType());
	}
}
