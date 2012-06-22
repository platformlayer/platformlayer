package org.platformlayer.service.platformlayer.ops;

import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.standardservice.StandardTemplateData;
import org.platformlayer.service.postgresql.model.PostgresqlServer;

public abstract class CommonTemplateData extends StandardTemplateData {

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

	protected abstract PlatformLayerKey getDatabaseKey();

	protected String getJdbcUrl() throws OpsException {
		PlatformLayerKey database = getDatabaseKey();
		PostgresqlServer item = platformLayer.getItem(database, PostgresqlServer.class);

		Machine itemMachine = instanceHelpers.getMachine(item);
		String host = itemMachine.getBestAddress(NetworkPoint.forTargetInContext(), 5432);

		return "jdbc:postgresql://" + host + ":5432/" + getDatabaseName();
	}

	public String getDatabaseName() {
		return "platformlayer";
	}

	@Override
	protected Properties getConfigurationProperties() throws OpsException {
		Properties properties = new Properties();
		properties.put("auth.jdbc.driverClassName", "org.postgresql.Driver");

		properties.put("auth.jdbc.url", getJdbcUrl());
		properties.put("auth.jdbc.username", getDatabaseUsername());
		properties.put("auth.jdbc.password", getDatabasePassword());

		return properties;
	}

}
