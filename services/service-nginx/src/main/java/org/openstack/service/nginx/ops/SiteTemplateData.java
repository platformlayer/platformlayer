package org.openstack.service.nginx.ops;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.openstack.service.nginx.model.NginxBackend;
import org.openstack.service.nginx.model.NginxFrontend;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.templates.TemplateDataSource;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class SiteTemplateData implements TemplateDataSource {
	@Inject
	InstanceHelpers instances;

	@Inject
	PlatformLayerHelpers platformLayer;

	public int httpPort = 80;
	public int httpsPort = 443;

	public static class BackendModel {
		public String address;
		public int port;

		public String getAddress() {
			return address;
		}

		public int getPort() {
			return port;
		}

	}

	NginxFrontend getNginxFrontend() {
		NginxFrontend nginxFrontend = OpsContext.get().getInstance(NginxFrontend.class);
		return nginxFrontend;
	}

	@Override
	public void buildTemplateModel(Map<String, Object> model) throws OpsException {
		NginxFrontend nginxFrontend = getNginxFrontend();

		model.put("uniqueKey", getUniqueKey());
		model.put("hostname", getHostname());
		model.put("httpPort", httpPort);
		model.put("httpsPort", httpsPort);
		model.put("useSsl", false);

		List<NginxBackend> backends = getBackends(nginxFrontend.hostname);
		List<BackendModel> resolvedBackends = resolveBackends(backends);

		model.put("backends", resolvedBackends);
	}

	String getUniqueKey() {
		String uniqueKey = getHostname();
		uniqueKey = sanitize(uniqueKey);
		return uniqueKey;
	}

	String getHostname() {
		return getNginxFrontend().hostname;
	}

	private List<BackendModel> resolveBackends(List<NginxBackend> backends) throws OpsException {
		List<BackendModel> resolved = Lists.newArrayList();
		for (NginxBackend backend : backends) {
			ItemBase backendItem = platformLayer.getItem(backend.backend);

			Machine backendMachine = instances.getMachine(backendItem);

			int port = 0;
			if (port == 0) {
				port = 80;
			}

			// if (address.contains(":")) {
			// resolved.addAll(resolvePlatformLayer(address));
			// continue;
			// }

			// TODO: We need to register a dependency on the resolved item

			BackendModel model = new BackendModel();
			model.address = backendMachine.getNetworkPoint().getBestAddress(NetworkPoint.forTargetInContext());
			model.port = port;
			resolved.add(model);
		}
		return resolved;
	}

	// private Collection<? extends BackendModel> resolvePlatformLayer(String spec) throws OpsException {
	// // e.g. jenkins:jenkinsService:123
	// String[] components = spec.split(":");
	// if (components.length != 3) {
	// throw new OpsException("Cannot resolve (illegal format): " + spec);
	// }
	//
	// ModelKey modelKey = new ModelKey(new ServiceType(components[0]), new ItemType(components[1]),
	// platformLayer.getProjectId(), new ManagedItemId(components[2]));
	//
	// List<BackendModel> resolved = Lists.newArrayList();
	//
	// UntypedItem item = platformLayer.getUntypedItem(modelKey.getServiceType(), modelKey.getItemType(),
	// modelKey.getItemKey());
	// if (item == null) {
	// throw new OpsException("Cannot resolve (not found): " + spec);
	// }
	//
	// Machine machine = instances.findMachine(item.getTags(), modelKey);
	// if (machine == null) {
	// throw new OpsException("Cannot resolve (no machine): " + spec);
	// }
	//
	// // TODO: We need to register a dependency on the resolved item
	//
	// BackendModel model = new BackendModel();
	// model.address = machine.getAddress();
	// model.port = 8080;
	//
	// resolved.add(model);
	//
	// return resolved;
	//
	// }

	private List<NginxBackend> getBackends(String hostname) throws OpsException {
		// TODO: This is not very efficient!!
		List<NginxBackend> backends = Lists.newArrayList();
		for (NginxBackend managedBackend : platformLayer.listItems(NginxBackend.class)) {
			if (Objects.equal(managedBackend.hostname, hostname)) {
				backends.add(managedBackend);
			}
		}
		return backends;
	}

	private String sanitize(String s) {
		s = s.replace('.', '_');
		return s;
	}

	public File getNginxEnabledConfigFile() {
		String uniqueKey = getUniqueKey();

		File sitesEnabledDir = new File("/etc/nginx/sites-enabled");
		File enabledSite = new File(sitesEnabledDir, uniqueKey + ".conf");
		return enabledSite;
	}

	public File getNginxAvailableConfigFile() {
		String uniqueKey = getUniqueKey();

		File sitesAvailableDir = new File("/etc/nginx/sites-available");
		File availableSite = new File(sitesAvailableDir, uniqueKey + ".conf");

		return availableSite;
	}

}
