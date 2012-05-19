package org.platformlayer.service.wordpress.ops;

import java.io.File;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.templates.TemplateDataSource;
import org.platformlayer.service.mysql.model.MysqlServer;
import org.platformlayer.service.wordpress.model.WordpressService;

public class WordpressTemplateData implements TemplateDataSource {
	static final Logger log = Logger.getLogger(WordpressTemplateData.class);

	private static final boolean USE_DNS_NAME = false;

	@Inject
	InstanceHelpers instanceHelpers;

	@Inject
	PlatformLayerHelpers platformLayerHelpers;

	public String getDomainName() {
		return getModel().dnsName;
	}

	private WordpressService getModel() {
		return OpsContext.get().getInstance(WordpressService.class);
	}

	@Override
	public void buildTemplateModel(Map<String, Object> model) throws OpsException {
		// TODO: Use bean binding?
		model.put("domainName", getDomainName());
		model.put("databaseName", getDatabaseName());
		model.put("databaseUser", getDatabaseUser());
		model.put("databasePassword", getDatabasePassword());
		model.put("databaseHost", getDatabaseHost());
		model.put("uploadPath", getUploadPath().toString());
		model.put("secretKey", getSecretKey());
	}

	private Secret getSecretKey() {
		return getModel().wordpressSecretKey;
	}

	private String getDatabaseHost() throws OpsException {
		MysqlServer item = platformLayerHelpers.getItem(getModel().databaseItem, MysqlServer.class);

		if (USE_DNS_NAME) {
			return item.dnsName;
		} else {
			Machine itemMachine = instanceHelpers.getMachine(item);
			return itemMachine.getBestAddress(NetworkPoint.forTargetInContext(), 3306);
		}
	}

	private File getUploadPath() {
		File uploadBase = new File("/src/www/wp-uploads");
		File uploadPath = new File(uploadBase, getDomainName());
		return uploadPath;
	}

	public String getDatabaseName() {
		return "wp_" + getDomainName().replace('.', '_');
	}

	public String getDatabaseUser() {
		String name = getDatabaseName();

		if (name.length() > 16) {
			// Mysql names are limited to 16 characters!!!
			log.warn("Truncating mysql user name to 16 characters: " + name);
			name = name.substring(0, 16);
		}

		return name;
	}

	public Secret getDatabasePassword() {
		return getModel().databasePassword;
	}
}
