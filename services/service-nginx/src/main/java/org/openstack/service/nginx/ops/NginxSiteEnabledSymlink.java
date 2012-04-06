package org.openstack.service.nginx.ops;

import java.io.File;

import javax.inject.Inject;

import org.platformlayer.ops.filesystem.ManagedSymlink;

public class NginxSiteEnabledSymlink extends ManagedSymlink {

    @Inject
    SiteTemplateData siteTemplateData;

    @Override
    protected File getSymlinkTarget() {
        return siteTemplateData.getNginxAvailableConfigFile();
    }

    @Override
    protected File getFilePath() {
        return siteTemplateData.getNginxEnabledConfigFile();
    }

}
