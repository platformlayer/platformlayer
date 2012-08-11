package org.platformlayer.service.dns.ops;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.java.JavaCommandBuilder;
import org.platformlayer.ops.standardservice.StandardTemplateData;
import org.platformlayer.service.dns.model.DnsServer;

import com.google.common.collect.Maps;

public class DnsServerTemplate extends StandardTemplateData {
	static final Logger log = Logger.getLogger(DnsServerTemplate.class);

	static final File BASE_DIR = new File("/var/dns");

	@Override
	public File getInstanceDir() {
		return BASE_DIR;
	}

	public static File getZonesDir() {
		return new File(BASE_DIR, "zones");
	}

	@Override
	public DnsServer getModel() {
		return OpsContext.get().getInstance(DnsServer.class);
	}

	@Override
	public String getKey() {
		return "dns";
	}

	@Override
	protected Command getCommand() throws OpsException {
		JavaCommandBuilder command = new JavaCommandBuilder();
		command.addClasspathFolder(getInstallDir());
		command.setMainClass("com.fathomdb.dns.server.DnsServer");
		command.addDefine("logback.configurationFile", getLogConfigurationPath());

		return command.get();
	}

	@Override
	protected Map<String, String> getConfigurationProperties() throws OpsException {
		return Maps.newHashMap();
	}

	@Override
	public void buildTemplateModel(Map<String, Object> model) throws OpsException {
	}

	@Override
	protected PlatformLayerKey getSslKeyPath() {
		return null;
	}

	public File getLogConfigurationPath() {
		return new File(getInstanceDir(), "logback.xml");
	}
}
