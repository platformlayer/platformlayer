package org.platformlayer.xaas.web;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class PlatformLayerServer {
	static final Logger log = LoggerFactory.getLogger(PlatformLayerServer.class);

	public static URLClassLoader buildClassLoader(List<File> serviceDirs) {
		List<URL> urls = new ArrayList<URL>();
		for (File serviceDirBase : serviceDirs) {
			// for Lists.newArrayList(classLoader.getURLs());
			for (File serviceDir : serviceDirBase.listFiles()) {
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
		}

		ClassLoader parent = ClassLoader.getSystemClassLoader(); // .getParent();

		// for (File serviceDir : parent.get.get.listFiles()) {
		// if (!serviceDir.isDirectory()) {
		// continue;
		// }
		// File classesDir = new File(serviceDir, "target/classes");
		// if (!classesDir.isDirectory()) {
		// continue;
		// }
		// classesDir = classesDir.getAbsoluteFile();
		//
		// try {
		// urls.add(classesDir.toURI().toURL());
		// log.info("Added " + classesDir);
		// } catch (MalformedURLException e) {
		// throw new IllegalStateException("Error processing directory: " + classesDir, e);
		// }
		// }

		URLClassLoader extended = URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]), parent);
		return extended;
	}

	public static void main(String[] args) throws Exception {
		// System.setProperty("application.mode", "development");

		File base = new File(".");

		String serviceClasspath = System.getProperty("serviceClasspath", "../../services");

		List<File> serviceBases = Lists.newArrayList();
		for (String servicePath : serviceClasspath.split(":")) {
			File servicesBase;
			if (servicePath.startsWith("/")) {
				servicesBase = new File(servicePath);
			} else {
				servicesBase = new File(base, servicePath);
			}
			serviceBases.add(servicesBase);
		}
		URLClassLoader classLoader = buildClassLoader(serviceBases);

		// well-behaved Java packages work relative to the
		// context classloader. Others don't (like commons-logging)
		Thread.currentThread().setContextClassLoader(classLoader);

		Class<?> mainClass = classLoader.loadClass("org.platformlayer.xaas.web.StandaloneXaasWebserver");
		Method main = mainClass.getMethod("main", new Class[] { args.getClass() });

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
