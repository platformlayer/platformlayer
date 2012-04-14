package org.platformlayer.client.cli.commands;

import java.io.PrintWriter;

import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.core.model.ServiceInfo;

public class ListServices extends PlatformLayerCommandRunnerBase {
	public ListServices() {
		super("list", "services");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException {
		PlatformLayerClient client = getPlatformLayerClient();

		return client.listServices(true);
	}

	@Override
	public void formatRaw(Object o, PrintWriter writer) {
		Iterable<ServiceInfo> services = (Iterable<ServiceInfo>) o;
		for (ServiceInfo service : services) {
			for (String publicType : service.publicTypes) {
				writer.println(publicType);
			}
		}
	}

}
