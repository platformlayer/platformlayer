package org.platformlayer.ops.packages;

import java.util.List;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.process.ProcessExecution;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class AptPackageManager {
	public static List<String> getInstalledPackageInfo(OpsTarget server) throws OpsException {
		Command command = Command.build("/usr/bin/dpkg --get-selections");
		ProcessExecution execution = server.executeCommand(command);

		final List<String> packages = Lists.newArrayList();
		for (String line : Splitter.on("\n").split(execution.getStdOut())) {
			line = line.trim();
			if (line.isEmpty()) {
				continue;
			}

			List<String> tokens = Lists
					.newArrayList(Splitter.on(CharMatcher.WHITESPACE).omitEmptyStrings().split(line));
			if (tokens.size() != 2) {
				throw new OpsException("Error parsing line; expected 2 items: " + line);
			}
			String state = tokens.get(1);
			if (state.equals("install")) {
				packages.add(tokens.get(0));
			} else if (state.equals("deinstall")) {
				// Not installed (?)
			} else {
				throw new OpsException("Unknown package state in line: " + line);
			}
		}
		return packages;
	}

	// // some packages might need longer if the network is slow
	// private static Map<String, TimeSpan> packageTimeMap = new HashMap<String, TimeSpan>() {
	// {
	// put("sun-java6-jdk", new TimeSpan("30m"));
	// }
	// };
	//
	// public static void installPackage(OpsServer server, String packageName) throws OpsException {
	// SimpleBashCommand command = new SimpleBashCommand("/usr/bin/apt-get");
	// command.addLiteralArg("-q");
	// command.addLiteralArg("-y");
	// command.addLiteralArg("install");
	// command.addQuotedArg(packageName);
	//
	// TimeSpan timeSpan = packageTimeMap.get(packageName);
	// if (timeSpan == null)
	// timeSpan = new TimeSpan("10m");
	//
	// server.simpleRun(command, timeSpan);
	// }
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
	//
	// public static List<String> findOutOfDatePackages(OpsServer server) throws OpsException {
	// SimpleBashCommand command = new SimpleBashCommand("/usr/bin/apt-get");
	// command.addLiteralArg("-q");
	// command.addLiteralArg("-u");
	// command.addLiteralArg("-s");
	// command.addLiteralArg("upgrade");
	//
	// final List<String> packages = Lists.newArrayList();
	//
	// ProcessExecution execution = server.simpleRun(command, new TimeSpan("15m"));
	// String stdout = execution.getStdOut();
	// Parsers.parseWhitespaceSeparated(execution.getStdOut(), new ParserCallback() {
	//
	// @Override
	// public void gotLine(List<String> items) throws Exception {
	// if (items.size() < 2)
	// return;
	// if ("Inst".equals(items.get(0))) {
	// String packageName = items.get(1);
	// packages.add(packageName);
	// }
	// }
	// });
	//
	// return packages;
	// }
}
