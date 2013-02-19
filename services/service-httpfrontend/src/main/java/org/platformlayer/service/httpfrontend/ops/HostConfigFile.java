package org.platformlayer.service.httpfrontend.ops;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.filesystem.SyntheticFile;
import org.platformlayer.ops.helpers.ProviderHelper;
import org.platformlayer.ops.helpers.ProviderHelper.ProviderOf;
import org.platformlayer.ops.http.HttpBackend;
import org.platformlayer.ops.machines.MachineProvider;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.machines.StorageConfiguration;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.service.httpfrontend.model.HttpServer;
import org.platformlayer.service.httpfrontend.model.HttpSite;

import com.fathomdb.Utf8;
import com.fathomdb.properties.PropertyUtils;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class HostConfigFile extends SyntheticFile {

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	HttpServerTemplateData template;

	@Inject
	ProviderHelper providers;

	@Override
	protected File getFilePath() {
		OpsContext ops = OpsContext.get();

		HttpSite site = ops.getInstance(HttpSite.class);
		return new File(template.getHostsDir(), site.hostname);
	}

	@Override
	protected byte[] getContentsBytes() throws OpsException {
		Properties properties = build();
		try {
			return Utf8.getBytes(PropertyUtils.serialize(properties));
		} catch (IOException e) {
			throw new OpsException("Error serializing properties", e);
		}
	}

	private Properties build() throws OpsException {
		Properties properties = new Properties();

		OpsContext ops = OpsContext.get();

		HttpServer server = ops.getInstance(HttpServer.class);
		HttpSite site = ops.getInstance(HttpSite.class);

		URI backendUri = URI.create(site.backend);
		if (backendUri.getScheme().equals("openstack")) {
			String host = backendUri.getHost();

			ProviderOf<MachineProvider> found = null;
			for (ProviderOf<MachineProvider> candidate : providers.listItemsProviding(MachineProvider.class)) {
				String itemIdString = candidate.getItem().getKey().getItemIdString();
				if (host.equals(itemIdString)) {
					if (found != null) {
						throw new OpsException("Host specifier is ambiguous: " + host);
					}
					found = candidate;
				}
			}

			if (found == null) {
				throw new OpsException("Cannot find backend cloud: " + backendUri);
			}

			MachineProvider machineProvider = found.get();
			StorageConfiguration storageConfiguration = machineProvider.getStorageConfiguration();

			Map<String, String> storageProperties = storageConfiguration.getProperties();
			storageProperties = PropertyUtils.prefixProperties(storageProperties, "openstack.");
			properties.putAll(storageProperties);

			String container = backendUri.getPath();
			if (container.startsWith("/")) {
				container = container.substring(1);
			}
			properties.put("openstack.container", container);

			properties.put("provider", "openstack");
		} else if (backendUri.getScheme().equals(PlatformLayerKey.SCHEME)) {
			PlatformLayerKey key = PlatformLayerKey.parse(site.backend);

			OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);
			NetworkPoint src = NetworkPoint.forTarget(target);

			List<String> backends = Lists.newArrayList();
			for (ProviderOf<HttpBackend> httpBackend : providers.listChildrenProviding(key, HttpBackend.class)) {
				backends.add(httpBackend.get().getUri(src).toString());
			}

			if (!backends.isEmpty()) {
				properties.put("backend", Joiner.on(",").join(backends));
			} else {
				throw new OpsException("No backends found!");
			}

			// properties.put("provider", "openstack");
		} else {
			throw new IllegalArgumentException("Unknown scheme: " + backendUri.getScheme());
		}

		return properties;
	}
}
