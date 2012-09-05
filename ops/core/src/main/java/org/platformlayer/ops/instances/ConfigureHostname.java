package org.platformlayer.ops.instances;

import java.io.File;

import org.platformlayer.ops.FileUpload;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OperationType;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.process.ProcessExecution;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

public class ConfigureHostname {
	public String hostname;

	@Handler({ OperationType.Delete })
	public void handleDelete() throws OpsException {
		// Do nothing
	}

	@Handler
	public void handle() throws OpsException {
		OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);

		if (Strings.isNullOrEmpty(hostname)) {
			throw new IllegalArgumentException();
		}

		// Fix hostname
		File hostsFile = new File("/etc/hosts");

		String hostLine = "127.0.0.1\t" + hostname;

		String hosts = target.readTextFile(hostsFile);

		boolean found = false;
		for (String line : Splitter.on("\n").trimResults().split(hosts)) {
			if (line.equals(hostLine)) {
				found = true;
			}
		}

		if (!found) {
			hosts += "\n" + hostLine + "\n";
			FileUpload.upload(target, hostsFile, hosts);
		}

		FileUpload.upload(target, new File("/etc/hostname"), hostname);

		{
			ProcessExecution execution = target.executeCommand("hostname");
			String currentHostname = execution.getStdOut().trim();
			if (!currentHostname.equals(hostname)) {
				// This actually can't be done within an LXC instance, which is why we go to extraordinary lengths to
				// set it on creation
				target.executeCommand("hostname {0}", hostname);
			}
		}
	}

	public static ConfigureHostname build(String hostname) {
		ConfigureHostname configureHostname = Injection.getInstance(ConfigureHostname.class);
		configureHostname.hostname = hostname;
		return configureHostname;
	}

}
