package org.platformlayer.service.wordpress.ops;

import java.io.File;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.SyntheticFile;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.tree.OpsTreeBase;

public class WordpressApacheSite extends OpsTreeBase {

	@Handler
	public void handler() {
	}

	public static WordpressApacheSite build() {
		return Injection.getInstance(WordpressApacheSite.class);
	}

	@Override
	protected void addChildren() throws OpsException {
		WordpressTemplateData templateData = Injection.getInstance(WordpressTemplateData.class);

		String domainName = templateData.getDomainName();

		File configBase = new File("/etc/wordpress");
		File configFile = new File(configBase, "htaccess");
		addChild(TemplatedFile.build(templateData, configFile).setOwner("root").setGroup("www-data").setFileMode("640"));

		String siteName = domainName;

		File sitesAvailable = new File("/etc/apache2/sites-available");
		File siteFile = new File(sitesAvailable, siteName);
		addChild(TemplatedFile.build(templateData, siteFile,
				SyntheticFile.getDefaultResourceName(getClass(), "apache-site")));

		addChild(ApacheSite.build(siteName));
	}
}
