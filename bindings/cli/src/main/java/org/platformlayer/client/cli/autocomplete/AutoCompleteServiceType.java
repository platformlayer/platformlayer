package org.platformlayer.client.cli.autocomplete;

import java.util.List;
import java.util.Map;

import org.platformlayer.client.cli.PlatformLayerCliContext;
import org.platformlayer.core.model.ServiceInfo;

import com.fathomdb.cli.CliContext;
import com.google.common.collect.Lists;

public class AutoCompleteServiceType extends PlatformLayerSimpleAutoCompleter {

	@Override
	public List<String> doComplete(CliContext context, String prefix) throws Exception {
		Map<String, ServiceInfo> services = ((PlatformLayerCliContext) context).listServices();

		List<String> serviceTypes = Lists.newArrayList(services.keySet());
		addSuffix(serviceTypes, " ");
		return serviceTypes;
	}

}
