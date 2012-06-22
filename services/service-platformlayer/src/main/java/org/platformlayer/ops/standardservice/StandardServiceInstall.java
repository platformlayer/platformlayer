package org.platformlayer.ops.standardservice;

import java.io.File;

import javax.inject.Inject;

import org.openstack.crypto.Md5Hash;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.DownloadFileByHash;
import org.platformlayer.ops.filesystem.ExpandArchive;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.java.JavaVirtualMachine;
import org.platformlayer.ops.supervisor.SupervisordService;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.ops.users.PosixGroup;
import org.platformlayer.ops.users.PosixUser;

public abstract class StandardServiceInstall extends OpsTreeBase {
	@Inject
	ServiceContext service;

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		StandardTemplateData template = getTemplate();

		addChild(injected(SupervisordService.class));

		String user = template.getUser();
		String group = template.getGroup();

		addChild(PosixGroup.build(user));
		addChild(PosixUser.build(user, false, group));

		File installDir = template.getInstallDir();
		File basePath = installDir;

		{
			// TODO: Auto-update this?? Add JenkinsLatest?
			Md5Hash hash = getMd5Hash();

			File zipFile = new File(basePath, template.getKey() + ".tar.gz");

			// TODO: Cache?
			DownloadFileByHash download = addChild(DownloadFileByHash.class);
			download.hash = hash;
			download.filePath = zipFile;

			// TODO: Only unzip if newly downloaded
			ExpandArchive unzip = addChild(ExpandArchive.class);
			unzip.archiveFile = zipFile;
			unzip.extractPath = installDir;
		}

		addChild(JavaVirtualMachine.buildJre7());
	}

	protected abstract Md5Hash getMd5Hash();

	protected abstract StandardTemplateData getTemplate();
}
