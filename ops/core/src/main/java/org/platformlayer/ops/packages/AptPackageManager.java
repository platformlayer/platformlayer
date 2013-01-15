package org.platformlayer.ops.packages;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.platformlayer.TimeSpan;
import org.platformlayer.images.model.ConfigurePackage;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.CommandEnvironment;
import org.platformlayer.ops.FileUpload;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.process.ProcessExecution;
import org.platformlayer.ops.process.ProcessExecutionException;
import org.platformlayer.ops.proxy.HttpProxyHelper;
import org.platformlayer.ops.proxy.HttpProxyHelper.Usage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class AptPackageManager {
	static final Logger log = LoggerFactory.getLogger(AptPackageManager.class);

	@Inject
	HttpProxyHelper httpProxies;

	class AptInfoCache {
		public List<String> installedPackages;
	}

	final Map<OpsTarget, AptInfoCache> cache = Maps.newHashMap();

	AptInfoCache getCache(OpsTarget target) {
		AptInfoCache cached = cache.get(target);
		if (cached == null) {
			cached = new AptInfoCache();
			cache.put(target, cached);
		}
		return cached;
	}

	void flushCache(OpsTarget target) {
		cache.remove(target);
	}

	public void install(OpsTarget target, String... packageNames) throws OpsException {
		install(target, Arrays.asList(packageNames));
	}

	public void upgrade(OpsTarget target) throws OpsException {
		CommandEnvironment commandEnvironment = buildEnvironmentWithProxy(target);

		Command command = Command.build("apt-get --yes upgrade");
		target.executeCommand(command.setEnvironment(commandEnvironment).setTimeout(TimeSpan.TEN_MINUTES));

		flushCache(target);
	}

	public void clean(OpsTarget target) throws OpsException {
		CommandEnvironment commandEnvironment = buildEnvironmentWithProxy(target);

		Command command = Command.build("apt-get clean");
		target.executeCommand(command.setEnvironment(commandEnvironment).setTimeout(TimeSpan.TEN_MINUTES));
	}

	private boolean haveCurl(OpsTarget target) throws OpsException {
		return target.getFilesystemInfoFile(new File("/usr/bin/curl")) != null;
	}

	public void addRepositoryKeyUrl(OpsTarget target, String url) throws OpsException {
		Command command = Command.build("wget -q -O - {0} | apt-key add -", url);
		target.executeCommand(command);

		flushCache(target);
	}

	public void addRepository(OpsTarget target, String id, List<String> sources) throws OpsException {
		File dir = new File("/etc/apt/sources.list.d");
		File file = new File(dir, id + ".list");

		StringBuilder sb = new StringBuilder();
		for (String source : sources) {
			sb.append(source);
			sb.append("\n");
		}

		log.info("Uploading to " + file + ": " + sb.toString());

		FileUpload.upload(target, file, sb.toString());

		flushCache(target);
	}

	public void preconfigurePackages(OpsTarget target, List<ConfigurePackage> settings) throws OpsException {
		File preseedTmpDir = target.createTempDir();

		StringBuilder sb = new StringBuilder();

		for (ConfigurePackage conf : settings) {
			String confType = conf.getType();
			if (confType == null) {
				confType = "string";
			}
			String line = conf.getPackageName() + "\t" + conf.getKey() + "\t" + conf.getType() + "\t" + conf.getValue()
					+ "\n";
			sb.append(line);
		}

		File preseedFile = new File(preseedTmpDir, "misc.preseed");

		FileUpload.upload(target, preseedFile, sb.toString());
		target.executeCommand(Command.build("cat {0} | debconf-set-selections", preseedFile));
	}

	public List<String> getInstalledPackageInfo(OpsTarget target) throws OpsException {
		AptInfoCache cached = getCache(target);

		if (cached.installedPackages == null) {
			Command command = Command.build("/usr/bin/dpkg --get-selections");
			ProcessExecution execution = target.executeCommand(command);

			final List<String> packages = Lists.newArrayList();
			for (String line : Splitter.on("\n").split(execution.getStdOut())) {
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}

				List<String> tokens = Lists.newArrayList(Splitter.on(CharMatcher.WHITESPACE).omitEmptyStrings()
						.split(line));
				if (tokens.size() != 2) {
					throw new OpsException("Error parsing line; expected 2 items: " + line);
				}
				String state = tokens.get(1);
				if (state.equals("install")) {
					String packageName = tokens.get(0);
					int colonIndex = packageName.indexOf(':');
					if (colonIndex != -1) {
						// Architecture sometimes follows package name
						packageName = packageName.substring(0, colonIndex);
					}
					packages.add(packageName);
				} else if (state.equals("deinstall")) {
					// Not installed (?)
				} else {
					throw new OpsException("Unknown package state in line: " + line);
				}
			}
			cached.installedPackages = packages;
		} else {
			log.debug("Re-using cached package info");
		}

		return cached.installedPackages;
	}

	// // some packages might need longer if the network is slow
	// private static Map<String, TimeSpan> packageTimeMap = new HashMap<String, TimeSpan>() {
	// {
	// put("sun-java6-jdk", new TimeSpan("30m"));
	// }
	// };
	//

	// public static void update(OpsServer server) throws OpsException {
	// SimpleBashCommand command = new SimpleBashCommand("/usr/bin/apt-get");
	// command.addLiteralArg("-q");
	// command.addLiteralArg("-y");
	// command.addLiteralArg("update");
	//
	// server.simpleRun(command, new TimeSpan("5m"));
	// }
	//
	// public static void installUpdates(OpsServer server) throws OpsException {
	// SimpleBashCommand command = new SimpleBashCommand("/usr/bin/apt-get");
	// command.addLiteralArg("-q");
	// command.addLiteralArg("-y");
	// command.addLiteralArg("upgrade");
	//
	// server.simpleRun(command, new TimeSpan("15m"));
	// }

	public List<String> findOutOfDatePackages(OpsTarget target) throws OpsException {
		Command command = Command.build("apt-get -q -q --simulate dist-upgrade");
		ProcessExecution execution = target.executeCommand(command);

		final List<String> packages = Lists.newArrayList();
		for (String line : Splitter.on("\n").split(execution.getStdOut())) {
			line = line.trim();
			if (line.isEmpty()) {
				continue;
			}

			List<String> tokens = Lists
					.newArrayList(Splitter.on(CharMatcher.WHITESPACE).omitEmptyStrings().split(line));
			if (tokens.size() < 2) {
				continue;
			}

			String action = tokens.get(0);
			if (action.equals("Inst")) {
				// e.g. Inst coreutils [8.13-3.1] (8.13-3.2 Debian:testing [amd64])
				packages.add(tokens.get(1));

				// New version is in tokens[2]
				// Current version is in tokens[3], but that's a trick
			}

		}
		return packages;
	}

	private CommandEnvironment buildEnvironmentWithProxy(OpsTarget target) throws OpsException {
		CommandEnvironment commandEnvironment = null;

		if (!haveCurl(target)) {
			log.warn("We don't yet have curl; can't detect best proxy so won't use a proxy");
		} else {
			commandEnvironment = httpProxies.getHttpProxyEnvironment(target, Usage.SoftwarePackages, null);
		}

		if (commandEnvironment == null) {
			commandEnvironment = new CommandEnvironment();
		}

		return commandEnvironment;
	}

	public void install(OpsTarget target, Iterable<String> packageNames) throws OpsException {
		CommandEnvironment commandEnvironment = buildEnvironmentWithProxy(target);
		commandEnvironment.put("DEBIAN_FRONTEND", "noninteractive");

		log.info("Installing APT packages: " + Joiner.on(",").join(packageNames));

		Command command = Command.build("apt-get install --yes");
		for (String packageName : packageNames) {
			command.addQuoted(packageName);
		}
		target.executeCommand(command.setEnvironment(commandEnvironment).setTimeout(TimeSpan.TEN_MINUTES));

		flushCache(target);
	}

	public void update(OpsTarget target, boolean force) throws OpsException {
		if (!force && !isUpdateNeeded(target)) {
			log.info("apt-get update not needed; won't update");
			return;
		}

		log.info("Updating apt repositories");

		CommandEnvironment commandEnvironment = buildEnvironmentWithProxy(target);

		Command command = Command.build("apt-get --yes update");
		command = command.setEnvironment(commandEnvironment).setTimeout(TimeSpan.TEN_MINUTES);
		executeAptCommand(target, command);

		flushCache(target);
	}

	private boolean isUpdateNeeded(OpsTarget target) {
		log.info("isUpdateNeeded stub-implemented");
		return true;
	}

	private void executeAptCommand(OpsTarget target, Command command) throws OpsException {
		for (int attempt = 0; attempt < 2; attempt++) {
			try {
				target.executeCommand(command);
			} catch (ProcessExecutionException e) {
				if (attempt == 0) {
					ProcessExecution execution = e.getExecution();
					if (execution != null) {
						String stdErr = execution.getStdErr();
						// Stderr=E: dpkg was interrupted, you must manually run 'dpkg --configure -a' to correct the
						// problem.
						if (stdErr.contains("dpkg --configure -a")) {
							log.warn("Detected that dpkg --configure -a is needed");
							doDpkgConfigure(target);
							log.warn("Retrying apt command");
							continue;
						}
					}
				}
				throw new OpsException("Error executing apt command", e);
			}
			return;
		}
	}

	private void doDpkgConfigure(OpsTarget target) throws OpsException {
		CommandEnvironment commandEnvironment = buildEnvironmentWithProxy(target);
		Command command = Command.build("dpkg --configure -a");
		command = command.setEnvironment(commandEnvironment).setTimeout(TimeSpan.TEN_MINUTES);
		target.executeCommand(command);
	}

}
