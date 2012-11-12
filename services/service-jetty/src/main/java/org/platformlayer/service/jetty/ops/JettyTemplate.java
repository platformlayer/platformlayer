package org.platformlayer.service.jetty.ops;

import java.io.File;
import java.util.Map;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Command.Argument;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.java.JavaCommandBuilder;
import org.platformlayer.ops.standardservice.StandardTemplateData;
import org.platformlayer.service.jetty.model.JettyService;

import com.google.common.collect.Maps;

public class JettyTemplate extends StandardTemplateData {

	@Override
	public void buildTemplateModel(Map<String, Object> model) throws OpsException {

	}

	public File getWarsStagingDir() {
		return new File(getBaseDir(), "wars");
	}

	public File getWarsDeployDir() {
		return new File(getBaseDir(), "webapps");
	}

	public File getBaseDir() {
		return getInstanceDir(); // new File("/var/lib/jetty");
	}

	@Override
	public JettyService getModel() {
		JettyService model = OpsContext.get().getInstance(JettyService.class);
		return model;
	}

	@Override
	public String getKey() {
		return "jetty";
	}

	@Override
	protected Command getCommand() {
		// We have problems with shell scripts that exec...
		// File shellScript = new File(getInstanceDir(), "bin/jetty.sh");
		// return Command.build("{0} run", shellScript);

		// /usr/bin/java -Djetty.home=/var/jetty/default -Djava.io.tmpdir=/tmp -jar /var/jetty/default/start.jar
		// --pre=etc/jetty-logging.xml

		JavaCommandBuilder command = new JavaCommandBuilder();
		command.addClasspathFolder(getInstallDir());
		command.addDefine("jetty.home", getBaseDir());
		command.addDefine("java.io.tmpdir", "/tmp");
		command.setJar(new File(getBaseDir(), "start.jar"));
		command.addArgument(Argument.buildLiteral("--pre=etc/jetty-logging.xml"));
		return command.get();
	}

	@Override
	protected Map<String, String> getConfigurationProperties() throws OpsException {
		return Maps.newHashMap();
	}

	@Override
	protected PlatformLayerKey getSslKeyPath() {
		return getModel().sslKey;
	}

	public File getExpandedDir() {
		return new File(getInstallDir(), "jetty-distribution-7.6.5.v20120716");
	}

}
