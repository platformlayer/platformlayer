package org.platformlayer.service.httpfrontend.ops;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;

import com.fathomdb.Utf8;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.filesystem.SyntheticFile;
import org.platformlayer.ops.helpers.ProviderHelper;
import org.platformlayer.ops.helpers.ProviderHelper.ProviderOf;
import org.platformlayer.ops.http.HttpBackend;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.service.httpfrontend.model.HttpServer;
import org.platformlayer.service.httpfrontend.model.HttpSite;
import org.platformlayer.service.machines.openstack.v1.OpenstackCloud;

import com.fathomdb.properties.PropertyUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
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
			OpenstackCloud cloud = platformLayer.findItem(backendUri.getHost(), OpenstackCloud.class);
			if (cloud == null) {
				throw new OpsException("Cannot find backend cloud: " + backendUri);
			}

			properties.put("openstack.url", cloud.getEndpoint());
			properties.put("openstack.user", cloud.getUsername());
			properties.put("openstack.key", cloud.getPassword());
			if (!Strings.isNullOrEmpty(cloud.getTenant())) {
				properties.put("openstack.tenant", cloud.getTenant());
			}

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
