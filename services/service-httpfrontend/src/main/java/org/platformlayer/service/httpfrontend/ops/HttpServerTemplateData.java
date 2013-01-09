package org.platformlayer.service.httpfrontend.ops;

import java.io.File;
import java.util.Map;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Command.Argument;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.java.JavaCommandBuilder;
import org.platformlayer.ops.standardservice.StandardTemplateData;
import org.platformlayer.service.httpfrontend.model.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class HttpServerTemplateData extends StandardTemplateData {

	private static final Logger log = LoggerFactory.getLogger(HttpServerTemplateData.class);

	// public File getInstanceDir() {
	// return HttpHelpers.PATH_BASE;
	// }

	@Override
	public void buildTemplateModel(Map<String, Object> model) throws OpsException {
		model.put("instanceDir", getInstanceDir());
		model.put("installDir", getInstallDir());
	}

	// public File getInstallDir() {
	// return new File("/opt/httpfrontend");
	// }

	public File getHostsDir() {
		return new File(getInstanceDir(), "hosts");
	}

	// public String getUser() {
	// // TODO: We'd prefer an unprivileged user, but we have to bind to a privileged port
	// // return "http";
	//
	// return null;
	// }

	@Override
	public Command getCommand() {
		JavaCommandBuilder command = new JavaCommandBuilder();
		command.addClasspathFolder(getInstallDir());
		command.addDefine("logback.configurationFile", getLogConfigurationFile());
		command.setMainClass("com.fathomdb.proxy.http.server.HttpProxyServer");
		command.addArgument(Argument.buildLiteral("80"));
		return command.get();
	}

	public File getLogConfigurationFile() {
		return new File(getInstanceDir(), "logback.xml");
	}

	// public Map<String, String> getEnvironment() {
	// return Maps.newHashMap();
	// }

	// @Inject
	// DatabaseHelper databases;

	@Override
	public HttpServer getModel() {
		HttpServer model = OpsContext.get().getInstance(HttpServer.class);
		return model;
	}

	@Override
	public String getKey() {
		return "httpfrontend";
	}

	@Override
	protected Map<String, String> getConfigurationProperties() throws OpsException {
		Map<String, String> properties = Maps.newHashMap();

		return properties;
	}

	@Override
	protected PlatformLayerKey getSslKeyPath() {
		return null;
	}

	@Override
	public boolean shouldCreateSslKey() {
		return false;
	}

}
