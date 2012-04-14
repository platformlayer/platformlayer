package org.openstack.service.nginx.ops;

import java.io.File;

import javax.inject.Inject;

import org.platformlayer.ops.filesystem.SyntheticFile;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.templates.TemplateDataSource;

public class NginxSiteConfig extends TemplatedFile {

	@Inject
	SiteTemplateData siteTemplateData;

	@Override
	protected File getFilePath() {
		return siteTemplateData.getNginxAvailableConfigFile();
	}

	@Override
	public TemplateDataSource getTemplateDataSource() {
		return siteTemplateData;
	}

	@Override
	public String getTemplateName() {
		return SyntheticFile.getDefaultResourceName(getClass(), "site.conf");
	}

}
