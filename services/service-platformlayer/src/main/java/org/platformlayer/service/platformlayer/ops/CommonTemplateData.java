package org.platformlayer.service.platformlayer.ops;

import java.util.Map;

import javax.inject.Inject;

import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.templates.TemplateDataSource;
import org.platformlayer.service.platformlayer.model.PlatformLayerService;
import org.platformlayer.service.postgresql.model.PostgresqlServer;

public class CommonTemplateData implements TemplateDataSource {

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	InstanceHelpers instanceHelpers;

	@Override
	public void buildTemplateModel(Map<String, Object> model) throws OpsException {
		model.put("jdbcUrl", getJdbcUrl());
		model.put("jdbcUsername", getDatabaseUsername());
		model.put("jdbcPassword", getDatabasePassword().plaintext());
	}

	public String getDatabaseUsername() {
		return "platformlayer_ops";
	}

	public Secret getDatabasePassword() {
		return Secret.build("platformlayer-password");
	}

	PlatformLayerService getPlatformLayerService() {
		return OpsContext.get().getInstance(PlatformLayerService.class);
	}

	private String getJdbcUrl() throws OpsException {
		PlatformLayerService model = getPlatformLayerService();
		PostgresqlServer item = platformLayer.getItem(model.database, PostgresqlServer.class);

		Machine itemMachine = instanceHelpers.getMachine(item);
		String host = itemMachine.getAddress(NetworkPoint.forTargetInContext(), 5432);

		return "jdbc:postgresql://" + host + ":5432/" + getDatabaseName();
	}

	public String getDatabaseName() {
		return "platformlayer";
	}

}
