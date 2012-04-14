package org.platformlayer.xaas.web;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class PlatformLayerServer {
	static final Logger log = Logger.getLogger(PlatformLayerServer.class);

	private static URLClassLoader buildClassLoader(File base) {
		List<URL> urls = new ArrayList<URL>();
		// for Lists.newArrayList(classLoader.getURLs());
		File servicesBase = new File(base, "../services");
		for (File serviceDir : servicesBase.listFiles()) {
			if (!serviceDir.isDirectory()) {
				continue;
			}
			File classesDir = new File(serviceDir, "target/classes");
			if (!classesDir.isDirectory()) {
				continue;
			}
			classesDir = classesDir.getAbsoluteFile();

			try {
				urls.add(classesDir.toURI().toURL());
				log.info("Added " + classesDir);
			} catch (MalformedURLException e) {
				throw new IllegalStateException("Error processing directory: " + classesDir, e);
			}
		}
		ClassLoader parent = ClassLoader.getSystemClassLoader(); // .getParent();
		URLClassLoader extended = URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]), parent);
		return extended;
	}

	public static void main(String[] args) throws Exception {
		System.setProperty("application.mode", "development");

		File base = new File(".");
		URLClassLoader classLoader = buildClassLoader(base);

		Class<?> mainClass = classLoader.loadClass("org.platformlayer.xaas.web.StandaloneXaasWebserver");
		Method main = mainClass.getMethod("main", new Class[] { args.getClass() });

		// well-behaved Java packages work relative to the
		// context classloader. Others don't (like commons-logging)
		Thread.currentThread().setContextClassLoader(classLoader);

		main.invoke(null, new Object[] { args });

		// try {
		// while (true) {
		// Thread.sleep(5000);
		// }
		// } finally {
		// server.stop();
		// }
	}

}
