package org.platformlayer.ops.helpers;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.TimeSpan;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.CommandEnvironment;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.HttpProxyHelper.Usage;
import org.platformlayer.service.imagefactory.v1.ConfigurePackage;

public class AptHelper {
	static final Logger log = Logger.getLogger(AptHelper.class);

	@Inject
	HttpProxyHelper httpProxies;

	public void install(OpsTarget target, String... packageNames) throws OpsException {
		CommandEnvironment commandEnvironment = buildEnvironmentWithProxy(target);

		commandEnvironment.add("DEBIAN_FRONTEND", "noninteractive");

		Command command = Command.build("apt-get install --yes");
		for (String packageName : packageNames) {
			command.addQuoted(packageName);
		}
		target.executeCommand(command.setEnvironment(commandEnvironment).setTimeout(TimeSpan.TEN_MINUTES));
	}

	private CommandEnvironment buildEnvironmentWithProxy(OpsTarget target) throws OpsException {
		CommandEnvironment commandEnvironment = null;

		if (!haveCurl(target)) {
			log.warn("We don't yet have curl; can't detect best proxy so won't use a proxy");
		} else {
			commandEnvironment = httpProxies.getHttpProxyEnvironment(target, Usage.SoftwarePackages);
		}

		if (commandEnvironment == null) {
			commandEnvironment = new CommandEnvironment();
		}

		return commandEnvironment;
	}

	public void update(OpsTarget target) throws OpsException {
		CommandEnvironment commandEnvironment = buildEnvironmentWithProxy(target);

		Command command = Command.build("apt-get --yes update");
		target.executeCommand(command.setEnvironment(commandEnvironment).setTimeout(TimeSpan.TEN_MINUTES));
	}

	public void upgrade(OpsTarget target) throws OpsException {
		CommandEnvironment commandEnvironment = buildEnvironmentWithProxy(target);

		Command command = Command.build("apt-get --yes upgrade");
		target.executeCommand(command.setEnvironment(commandEnvironment).setTimeout(TimeSpan.TEN_MINUTES));
	}

	private boolean haveCurl(OpsTarget target) throws OpsException {
		return target.getFilesystemInfoFile(new File("/usr/bin/curl")) != null;
	}

	public void addRepositoryKeyUrl(OpsTarget target, String url) throws OpsException {
		Command command = Command.build("wget -q -O - {0} | apt-key add -", url);
		target.executeCommand(command);
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

		target.setFileContents(file, sb.toString());
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

		target.setFileContents(preseedFile, sb.toString());
		target.executeCommand(Command.build("cat {0} | debconf-set-selections", preseedFile));
	}

}
