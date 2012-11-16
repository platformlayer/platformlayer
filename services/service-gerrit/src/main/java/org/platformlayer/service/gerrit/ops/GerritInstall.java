package org.platformlayer.service.gerrit.ops;

import java.io.File;

import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.DownloadFileByHash;
import org.platformlayer.ops.filesystem.ManagedSymlink;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.jetty.ops.JettyInstall;

import com.fathomdb.hash.Md5Hash;

public class GerritInstall extends OpsTreeBase {

	@Bound
	GerritTemplate template;

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		addChild(PackageDependency.build("git"));

		{
			JettyInstall jettyInstall = addChild(JettyInstall.class);
			jettyInstall.template = template;
		}

		{
			String url = "http://gerrit.googlecode.com/files/gerrit-full-2.5.war";

			DownloadFileByHash download = addChild(DownloadFileByHash.class);
			download.setUrl(url);
			download.hash = new Md5Hash("aa0e35045b2467cfff66c365a351ee9c");
			download.filePath = template.getInstallWarFile();

			// // Symlinks avoid multiple versions
			// parent.addChild(ManagedSymlink.build(template.getWarFile(), zipFile));
		}

		// GerritInstanceModel template = injected(GerritInstanceModel.class);

		// addChild(JavaVirtualMachine.buildJava7());

		// addChild(injected(JettyInstall.class));

		// addChild(injected(SupervisordService.class));

		//
		// addChild(PosixGroup.build("gerrit"));
		// addChild(PosixUser.build("gerrit", false, "gerrit"));

	}

	// @Override
	// protected DownloadFileByHash buildDownload() {
	// DownloadFileByHash download = super.buildDownload();
	//
	// // String url = "http://gerrit.googlecode.com/files/gerrit-2.4.2.war";
	// // download.setUrl(url);
	// // download.hash = new Md5Hash("53d41b68cb0b26be9e9fa9937322928b");
	//
	// String url = "http://gerrit.googlecode.com/files/gerrit-full-2.5.war";
	// download.setUrl(url);
	// download.hash = new Md5Hash("aa0e35045b2467cfff66c365a351ee9c");
	//
	// return download;
	// }

	public static void addLibExt(OpsTreeBase parent, GerritTemplate template) throws OpsException {
		File extDir = template.getLibExtDir(); // new File("/var/lib/jetty/lib/ext");

		{
			String url = "http://jdbc.postgresql.org/download/postgresql-9.1-903.jdbc4.jar";

			File basePath = template.getInstallDir();
			File zipFile = new File(basePath, "postgresql-9.1-903.jdbc4.jar");

			DownloadFileByHash download = parent.addChild(DownloadFileByHash.class);
			download.setUrl(url);
			download.hash = new Md5Hash("3222f2e4f133e8d1a76e9ba76463f8f5");
			download.filePath = zipFile;

			// Symlinks avoid multiple versions
			parent.addChild(ManagedSymlink.build(new File(extDir, "postgresql.jar"), zipFile));
		}

		{
			String url = "http://repo1.maven.org/maven2/commons-pool/commons-pool/1.6/commons-pool-1.6.jar";

			File basePath = template.getInstallDir();
			File zipFile = new File(basePath, "commons-pool-1.6.jar");

			DownloadFileByHash download = parent.addChild(DownloadFileByHash.class);
			download.setUrl(url);
			download.hash = new Md5Hash("5ca02245c829422176d23fa530e919cc");
			download.filePath = zipFile;

			// Symlinks avoid multiple versions
			parent.addChild(ManagedSymlink.build(new File(extDir, "commons-pool.jar"), zipFile));
		}

		{
			String url = "http://repo1.maven.org/maven2/commons-dbcp/commons-dbcp/1.4/commons-dbcp-1.4.jar";

			File basePath = template.getInstallDir();
			File zipFile = new File(basePath, "commons-dbcp-1.4.jar");

			DownloadFileByHash download = parent.addChild(DownloadFileByHash.class);
			download.setUrl(url);
			download.hash = new Md5Hash("b004158fab904f37f5831860898b3cd9");
			download.filePath = zipFile;

			// Symlinks avoid multiple versions
			parent.addChild(ManagedSymlink.build(new File(extDir, "commons-dbcp.jar"), zipFile));
		}

		{
			String url = "http://downloads.bouncycastle.org/java/bcprov-jdk15on-147.jar";

			File basePath = template.getInstallDir();
			File dest = new File(basePath, "bcprov-jdk15on-147.jar");

			DownloadFileByHash download = parent.addChild(DownloadFileByHash.class);
			download.setUrl(url);
			download.hash = new Md5Hash("7749dd7eca4403fb968ddc484263736a");
			download.filePath = dest;

			// Symlinks avoid multiple versions
			parent.addChild(ManagedSymlink.build(new File(extDir, "bcprov.jar"), dest));
		}
		//
		// {
		// String url = "http://www.bouncycastle.org/download/bcpg-jdk15on-147.jar";
		//
		// File basePath = template.getInstallDir();
		// File zipFile = new File(basePath, "bcpg-jdk15on-147.jar");
		//
		// DownloadFile download = injected(DownloadFile.class);
		// download.setUrl(url);
		// download.hash = new Md5Hash("1234ef4408bb015aa3a5eefcb1047aec");
		// download.filePath = zipFile;
		// addChild(download);
		//
		// // Symlinks avoid multiple versions
		// addChild(ManagedSymlink.build(new File(extDir, "bcpg.jar"), zipFile));
		// }

	}

}
