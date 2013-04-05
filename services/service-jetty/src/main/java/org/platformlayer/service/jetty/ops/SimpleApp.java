package org.platformlayer.service.jetty.ops;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;

import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsProvider;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.filesystem.DownloadFileByHash;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.filesystem.ManagedSymlink;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.standardservice.PropertiesConfigFile;
import org.platformlayer.ops.templates.TemplateDataSource;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.ops.uses.LinkHelpers;
import org.platformlayer.service.jetty.model.JettyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class SimpleApp extends OpsTreeBase {
	private static final Logger log = LoggerFactory.getLogger(SimpleApp.class);

	public String key;
	public JettyContext context;

	@Bound
	JettyTemplate jettyTemplate;

	@Inject
	LinkHelpers consumeHelper;

	public File getWorkDir() {
		File workDir = new File(jettyTemplate.getBaseDir(), "work/" + key);
		return workDir;
	}

	class ContextTemplate implements TemplateDataSource {

		@Override
		public void buildTemplateModel(Map<String, Object> model) throws OpsException {
			model.put("contextParameters", getContextParameters().entrySet());
		}

		public Map<String, String> getContextParameters() {
			Map<String, String> contextParameters = Maps.newHashMap();

			contextParameters.put("conf", getConfigurationFilePath().getAbsolutePath());
			contextParameters.put("org.eclipse.jetty.servlet.Default.dirAllowed", "false");

			return contextParameters;
		}
	}

	@Handler
	public void handler(OpsTarget target) throws IOException, OpsException {
	}

	public File getConfigurationFilePath() {
		return new File(getWorkDir(), "configuration.properties");
	}

	@Override
	protected void addChildren() throws OpsException {
		DownloadFileByHash download = addChild(buildDownload());
		File deployed = new File(jettyTemplate.getWarsDeployDir(), getWarName());
		addChild(ManagedSymlink.build(deployed, download.filePath));

		addChild(ManagedDirectory.build(getWorkDir(), "0700"));

		{
			PropertiesConfigFile conf = addChild(PropertiesConfigFile.class);
			conf.filePath = getConfigurationFilePath();
			conf.propertiesSupplier = new OpsProvider<Map<String, String>>() {
				@Override
				public Map<String, String> get() throws OpsException {
					return getConfigurationProperties();
				}
			};
		}

		File contextDir = jettyTemplate.getContextDir();
		ContextTemplate contextTemplate = new ContextTemplate();
		addChild(TemplatedFile.build(contextTemplate, new File(contextDir, "context.xml")));
	}

	protected Map<String, String> getConfigurationProperties() throws OpsException {
		Map<String, String> config = Maps.newHashMap();

		if (context.links != null) {
			config.putAll(consumeHelper.buildLinkTargetProperties(context.links.getLinks()));
		}

		return config;
	}

	protected DownloadFileByHash buildDownload() {
		DownloadFileByHash download = injected(DownloadFileByHash.class);
		download.filePath = new File(jettyTemplate.getWarsStagingDir(), getWarName());
		download.specifier = context.source;

		return download;
	}

	private String getWarName() {
		return key + ".war";
	}

}
