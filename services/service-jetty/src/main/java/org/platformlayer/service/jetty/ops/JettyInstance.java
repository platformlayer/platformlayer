package org.platformlayer.service.jetty.ops;

import java.io.File;

import org.platformlayer.ops.Bound;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.filesystem.ManagedSymlink;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.standardservice.StandardServiceInstance;
import org.platformlayer.ops.standardservice.StandardTemplateData;
import org.platformlayer.service.jetty.model.JettyContext;
import org.platformlayer.service.jetty.model.JettyService;

public class JettyInstance extends StandardServiceInstance {

	@Bound
	JettyService model;

	@Bound
	JettyTemplate template;

	@Override
	protected JettyTemplate getTemplate() {
		return template;
	}

	@Override
	protected void addConfigurationFile(final StandardTemplateData template) throws OpsException {
		// No config file just yet
	}

	@Override
	protected void addExtraFiles() throws OpsException {
		File instanceDir = template.getInstanceDir();

		// For now we symlink etc... we might have to copy it in future

		String[] links = { "bin", "etc", "lib", "start.jar" };

		File expandedDir = template.getExpandedDir();
		for (String link : links) {
			addChild(ManagedSymlink.build(new File(instanceDir, link), new File(expandedDir, link)));
		}

		String[] emptyDirs = { "contexts", "logs", "webapps", "wars" };
		for (String dir : emptyDirs) {
			addChild(ManagedDirectory.build(new File(instanceDir, dir), "755").setOwner(template.getUser())
					.setGroup(template.getGroup()));
		}

		addChild(TemplatedFile.build(template, new File(instanceDir, "start.ini")));

		AppsContainer apps = addChild(AppsContainer.class);

		if (model.contexts != null) {
			for (JettyContext context : model.contexts) {
				SimpleApp app = apps.addChild(SimpleApp.class);
				String key = context.id;
				if (key == null) {
					key = "root";
				}
				app.key = key;
				app.source = context.source;
			}
		}

		// JettyTemplate template = injected(JettyTemplate.class);
		// // We've set NO_START=0 in /etc/default/jetty, also listen host to 0.0.0.0
		// addChild(TemplatedFile.build(template, new File("/etc/default/jetty"), "etc.default.jetty"));

		// We used to add the app container here
		// addChild(ManagedService.build("jetty"));
	}

	public void addApp(Object app) throws OpsException {
		getChild(AppsContainer.class).addChild(app);
	}
}
