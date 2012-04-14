package org.platformlayer.guice;

import org.platformlayer.ids.ServiceType;
import org.platformlayer.xaas.services.ModelClass;

public class TableMapper {
	public static String toTableName(ModelClass<?> modelClass) {
		String tableName = modelClass.getProvider().getServiceType().getKey() + "_" + modelClass.getItemType().getKey();
		tableName = tableName.replace('-', '_');
		return "item_" + tableName;
	}

	public static String getAuthorizationTableName(ServiceType serviceType) {
		String tableName = serviceType.getKey();
		tableName = tableName.replace('-', '_');
		return "auth_" + tableName;
	}

	public static String getMetadataTableName(ServiceType serviceType) {
		String tableName = serviceType.getKey();
		tableName = tableName.replace('-', '_');
		return "meta_" + tableName;
	}
}
