//package org.platformlayer.service.gerrit.ops;
//
//import java.io.File;
//
//import org.platformlayer.ops.Handler;
//import org.platformlayer.ops.Injection;
//import org.platformlayer.ops.OpsContext;
//import org.platformlayer.ops.OpsException;
//import org.platformlayer.ops.OpsTarget;
//import org.platformlayer.ops.filesystem.ManagedSymlink;
//import org.platformlayer.ops.standardservice.StandardServiceInstance;
//import org.platformlayer.ops.tree.OpsTreeBase;
//import org.platformlayer.service.platformlayer.ops.auth.user.UserAuthInstanceModel;
//
//public class GerritWarInstance extends OpsTreeBase {
//	@Handler
//	public void handler() {
//	}
//
//	@Override
//	protected void addChildren() throws OpsException {
//		// TODO: This needs to be idempotent
//		OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);
//
//		File jettyBase = new File("/var/lib/jetty");
//		File warsBase = new File(jettyBase, "wars");
//		File webapps = new File(jettyBase, "webapps");
//
//		File installed = new File("/opt/gerrit/gerrit-2.4.2.war");
//
//		addChild(ManagedSymlink.build(new File(webapps, "root.war"), installed));
//
//		// String url = "http://nexus.sonatype.org/downloads/all/nexus-webapp-1.9.2.4.war";
//		// File warFile = new File("/var/lib/jetty/wars/nexus-webapp-1.9.2.4.war");
//		//
//		// target.executeCommand("wget {0} -O {1}", url, warFile);
//		//
//		// // Whatever version of nexus we have, we want it to be the root
//		// target.symlink(warFile, new File("/var/lib/jetty/webapps/root.war"), false);
//		//
//		// // TODO Auto-generated method stub
//		// throw new UnsupportedOperationException();
//	}
//
//	public static GerritWarInstance build() {
//		return Injection.getInstance(GerritWarInstance.class);
//	}
//
// }
