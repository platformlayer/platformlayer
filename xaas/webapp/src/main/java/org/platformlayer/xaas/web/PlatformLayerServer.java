package org.platformlayer.xaas.web;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class PlatformLayerServer {
	static final Logger log = LoggerFactory.getLogger(PlatformLayerServer.class);

	public static URLClassLoader buildClassLoader(List<File> serviceDirs) {
		List<URL> urls = new ArrayList<URL>();
		for (File serviceDirBase : serviceDirs) {
			if (!serviceDirBase.exists()) {
				throw new IllegalStateException("Directory not found: " + serviceDirBase);
			}
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

				File dependenciesDir = new File(serviceDir, "target/dependencies");
				if (!dependenciesDir.isDirectory()) {
					continue;
				}
				for (File file : dependenciesDir.listFiles()) {
					String name = file.getName();
					if (name.startsWith("fathomdb-"))
						continue;

					file = file.getAbsoluteFile();
					try {
						urls.add(file.toURI().toURL());
						log.info("Added " + file);
					} catch (MalformedURLException e) {
						throw new IllegalStateException("Error processing directory: " + classesDir, e);
					}
				}

			}
		}

		Set<String> done = Sets.newHashSet();

		for (File serviceDirBase : serviceDirs) {
			if (!serviceDirBase.exists()) {
				throw new IllegalStateException("Directory not found: " + serviceDirBase);
			}
			// for Lists.newArrayList(classLoader.getURLs());
			for (File serviceDir : serviceDirBase.listFiles()) {
				if (!serviceDir.isDirectory()) {
					continue;
				}

				File dependenciesDir = new File(serviceDir, "target/dependency");
				if (!dependenciesDir.isDirectory()) {
					continue;
				}

				List<String> prefixes = Lists.newArrayList();
				prefixes.add("platformlayer-");
				prefixes.add("fathomdb-");
				prefixes.add("gwt-");
				prefixes.add("logback-");
				prefixes.add("jackson-");
				// prefixes.add("jersey-");
				prefixes.add("jaxb-");
				prefixes.add("commons-");
				prefixes.add("javax.inject-");
				prefixes.add("validation-api-");
				prefixes.add("guava-");
				prefixes.add("gin-");
				prefixes.add("guice-");
				prefixes.add("junit-");
				prefixes.add("httpclient-");
				prefixes.add("httpcore-");
				prefixes.add("mina-");
				prefixes.add("sshd-");
				prefixes.add("freemarker-");
				prefixes.add("hibernate-");
				prefixes.add("jboss-");
				prefixes.add("service-");
				prefixes.add("netty-3");

				for (File file : dependenciesDir.listFiles()) {
					boolean veto = false;
					String name = file.getName();
					if (done.contains(name))
						continue;

					for (String prefix : prefixes) {
						if (name.startsWith(prefix)) {
							veto = true;
							break;
						}
					}
					if (veto)
						continue;

					file = file.getAbsoluteFile();
					try {
						urls.add(file.toURI().toURL());
						log.info("Added " + file);
						done.add(name);
					} catch (MalformedURLException e) {
						throw new IllegalStateException("Error processing directory: " + dependenciesDir, e);
					}
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
