package org.platformlayer.service.gerrit.ops;

import org.openstack.crypto.Md5Hash;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.DownloadFileByHash;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.standardservice.StandardServiceInstall;

public class GerritInstall extends StandardServiceInstall {

	@Override
	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		addChild(PackageDependency.build("git"));

		super.addChildren();
	}

	// // GerritInstanceModel template = injected(GerritInstanceModel.class);
	//
	// // addChild(JavaVirtualMachine.buildJava7());
	//
	// // addChild(injected(JettyInstall.class));
	//
	// // addChild(injected(SupervisordService.class));
	//
	// // File extDir = new File("/var/lib/jetty/lib/ext");
	// //
	// // {
	// // String url = "http://jdbc.postgresql.org/download/postgresql-9.1-902.jdbc4.jar";
	// //
	// // File basePath = template.getInstallDir();
	// // File zipFile = new File(basePath, "postgresql-9.1-902.jdbc4.jar");
	// //
	// // DownloadFile download = injected(DownloadFile.class);
	// // download.setUrl(url);
	// // download.hash = new Md5Hash("1234ef4408bb015aa3a5eefcb1047aec");
	// // download.filePath = zipFile;
	// // addChild(download);
	// //
	// // // Symlinks avoid multiple versions
	// // addChild(ManagedSymlink.build(new File(extDir, "postgresql.jar"), zipFile));
	// // }
	// //
	// // {
	// // String url = "http://repo1.maven.org/maven2/commons-pool/commons-pool/1.6/commons-pool-1.6.jar";
	// //
	// // File basePath = template.getInstallDir();
	// // File zipFile = new File(basePath, "commons-pool-1.6.jar");
	// //
	// // DownloadFile download = injected(DownloadFile.class);
	// // download.setUrl(url);
	// // download.hash = new Md5Hash("1234ef4408bb015aa3a5eefcb1047aec");
	// // download.filePath = zipFile;
	// // addChild(download);
	// //
	// // // Symlinks avoid multiple versions
	// // addChild(ManagedSymlink.build(new File(extDir, "commons-pool.jar"), zipFile));
	// // }
	// //
	// // {
	// // String url = "http://repo1.maven.org/maven2/commons-dbcp/commons-dbcp/1.4/commons-dbcp-1.4.jar";
	// //
	// // File basePath = template.getInstallDir();
	// // File zipFile = new File(basePath, "commons-dbcp-1.4.jar");
	// //
	// // DownloadFile download = injected(DownloadFile.class);
	// // download.setUrl(url);
	// // download.hash = new Md5Hash("1234ef4408bb015aa3a5eefcb1047aec");
	// // download.filePath = zipFile;
	// // addChild(download);
	// //
	// // // Symlinks avoid multiple versions
	// // addChild(ManagedSymlink.build(new File(extDir, "commons-dbcp.jar"), zipFile));
	// // }
	// //
	// // {
	// // String url = "http://www.bouncycastle.org/download/bcprov-jdk15on-147.jar";
	// //
	// // File basePath = template.getInstallDir();
	// // File dest = new File(basePath, "bcprov-jdk15on-147.jar");
	// //
	// // DownloadFile download = injected(DownloadFile.class);
	// // download.setUrl(url);
	// // download.hash = new Md5Hash("1234ef4408bb015aa3a5eefcb1047aec");
	// // download.filePath = dest;
	// // addChild(download);
	// //
	// // // Symlinks avoid multiple versions
	// // addChild(ManagedSymlink.build(new File(extDir, "bcprov.jar"), dest));
	// // }
	// //
	// // {
	// // String url = "http://www.bouncycastle.org/download/bcpg-jdk15on-147.jar";
	// //
	// // File basePath = template.getInstallDir();
	// // File zipFile = new File(basePath, "bcpg-jdk15on-147.jar");
	// //
	// // DownloadFile download = injected(DownloadFile.class);
	// // download.setUrl(url);
	// // download.hash = new Md5Hash("1234ef4408bb015aa3a5eefcb1047aec");
	// // download.filePath = zipFile;
	// // addChild(download);
	// //
	// // // Symlinks avoid multiple versions
	// // addChild(ManagedSymlink.build(new File(extDir, "bcpg.jar"), zipFile));
	// // }
	//
	// //
	// // addChild(PosixGroup.build("gerrit"));
	// // addChild(PosixUser.build("gerrit", false, "gerrit"));
	// }

	@Override
	protected GerritInstanceModel getTemplate() {
		return injected(GerritInstanceModel.class);
	}

	@Override
	protected DownloadFileByHash buildDownload() {
		DownloadFileByHash download = super.buildDownload();

		String url = "http://gerrit.googlecode.com/files/gerrit-2.4.2.war";

		download.setUrl(url);
		download.hash = new Md5Hash("53d41b68cb0b26be9e9fa9937322928b");

		return download;
	}
}
