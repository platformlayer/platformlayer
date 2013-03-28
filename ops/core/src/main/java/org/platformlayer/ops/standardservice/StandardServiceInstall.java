package org.platformlayer.ops.standardservice;

import java.io.File;

import javax.inject.Inject;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.DownloadFileByHash;
import org.platformlayer.ops.filesystem.ExpandArchive;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.java.JavaVirtualMachine;
import org.platformlayer.ops.supervisor.ServiceManager;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.ops.users.PosixGroup;
import org.platformlayer.ops.users.PosixUser;

public abstract class StandardServiceInstall extends OpsTreeBase {
	@Inject
	ServiceContext service;

	@Inject
	ServiceManager serviceManager;

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		StandardTemplateData template = getTemplate();

		serviceManager.addServiceInstall(template.getModel().getKey(), this);

		String user = template.getUser();
		String group = template.getGroup();

		addChild(PosixGroup.build(user));
		addChild(PosixUser.build(user, false, group));

		File installDir = template.getInstallDir();

		{
			DownloadFileByHash download = buildDownload();
			if (download != null) {
				addChild(download);
			}

			if (download != null && template.shouldExpand()) {
				// TODO: Only unzip if newly downloaded
				ExpandArchive unzip = addChild(ExpandArchive.class);
				unzip.archiveFile = download.filePath;
				unzip.extractPath = installDir;
			}
		}

		addChild(JavaVirtualMachine.buildJre7());
	}

	protected DownloadFileByHash buildDownload() {
		StandardTemplateData template = getTemplate();

		String specifier = template.getDownloadSpecifier();
		if (specifier == null) {
			return null;
		}

		// TODO: Auto-update this?? Add JenkinsLatest?

		File zipFile = template.getDistFile();

		// TODO: CAS / Cache?
		DownloadFileByHash download = injected(DownloadFileByHash.class);
		download.filePath = zipFile;
		download.specifier = specifier;

		return download;
	}

	protected abstract StandardTemplateData getTemplate();
}
