package org.platformlayer.ops.networks;

import java.util.List;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Command;

import com.google.common.collect.Lists;

public class ScriptBuilder {
	private static final Logger log = Logger.getLogger(ScriptBuilder.class);

	List<String> commands = Lists.newArrayList();

	public void addLiteral(String command) {
		commands.add(command);
	}

	public void add(Command command) {
		commands.add(command.buildCommandString());
	}

	public void add(String literal, Object... args) {
		Command command = Command.build(literal, args);
		add(command);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("#!/bin/bash\n");
		sb.append("set -e\n");
		for (String command : commands) {
			sb.append(command);
			sb.append("\n");
		}
		return sb.toString();
	}

}
