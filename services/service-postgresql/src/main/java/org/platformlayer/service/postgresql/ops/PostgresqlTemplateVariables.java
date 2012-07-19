package org.platformlayer.service.postgresql.ops;

import java.util.Map;

import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.templates.TemplateDataSource;
import org.platformlayer.service.postgresql.model.PostgresqlServer;

public class PostgresqlTemplateVariables implements TemplateDataSource {

	@Override
	public void buildTemplateModel(Map<String, Object> model) throws OpsException {

	}

	public PostgresqlServer getModel() {
		PostgresqlServer model = OpsContext.get().getInstance(PostgresqlServer.class);
		return model;
	}

	public String getPostgresVersion() {
		return "9.1";
	}
}
