package org.platformlayer.service.httpfrontend.ops;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Command.Argument;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.templates.TemplateDataSource;

public class HttpServerTemplateData implements TemplateDataSource {
	static final Logger log = Logger.getLogger(HttpServerTemplateData.class);

	public File getInstanceDir() {
		return HttpHelpers.PATH_BASE;
	}

	@Override
	public void buildTemplateModel(Map<String, Object> model) throws OpsException {
		model.put("instanceDir", getInstanceDir());
		model.put("installDir", getInstallDir());
	}

	public File getInstallDir() {
		return new File("/opt/httpfrontend");
	}

	public File getHostsDir() {
		return new File(getInstanceDir(), "hosts");
	}

	public String getUser() {
		// TODO: We'd prefer an unprivileged user, but we have to bind to a privileged port
		// return "http";

		return null;
	}

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

}
