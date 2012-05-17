package org.platformlayer.service.httpfrontend.ops;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import javax.inject.Inject;

import org.openstack.utils.PropertyUtils;
import org.openstack.utils.Utf8;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.SyntheticFile;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.service.httpfrontend.model.HttpServer;
import org.platformlayer.service.httpfrontend.model.HttpSite;
import org.platformlayer.service.machines.openstack.v1.OpenstackCloud;

import com.google.common.base.Strings;

public class HostConfigFile extends SyntheticFile {

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	HttpServerTemplateData template;

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

		URI backend = URI.create(site.backend);
		if (backend.getScheme().equals("openstack")) {
			OpenstackCloud cloud = platformLayer.findItem(backend.getHost(), OpenstackCloud.class);
			if (cloud == null) {
				throw new OpsException("Cannot find backend cloud: " + backend);
			}

			properties.put("openstack.url", cloud.getEndpoint());
			properties.put("openstack.user", cloud.getUsername());
			properties.put("openstack.key", cloud.getPassword());
			if (!Strings.isNullOrEmpty(cloud.getTenant())) {
				properties.put("openstack.tenant", cloud.getTenant());
			}

			String container = backend.getPath();
			if (container.startsWith("/")) {
				container = container.substring(1);
			}
			properties.put("openstack.container", container);
		} else {
			throw new IllegalArgumentException("Unknown scheme: " + backend.getScheme());
		}

		return properties;
	}
}
