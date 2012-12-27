package org.platformlayer.ops.networks;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import com.fathomdb.Utf8;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.firewall.Sanitizer;
import org.platformlayer.ops.firewall.Sanitizer.Decision;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ScriptBuilder {
	private static final Logger log = Logger.getLogger(ScriptBuilder.class);

	final List<String> commands = Lists.newArrayList();
	final Map<String, String> metadata = Maps.newHashMap();

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

	public void addMetadata(String key, String value) {
		metadata.put(key, value);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("#!/bin/bash\n");

		sb.append("# Created / managed by PlatformLayer. Do not edit.\n");
		sb.append("# __PLATFORMLAYER__METADATA__BEGIN__\n");
		Sanitizer sanitizer = new Sanitizer(Decision.Throw, '_');
		sanitizer.allowAlphanumeric();
		sanitizer.allowCharacters("!@$%^&*()[]{}_-+|<>,.");
		sanitizer.setDecision("#= ", Decision.Throw);
		for (Entry<String, String> entry : metadata.entrySet()) {
			String key = sanitizer.clean(entry.getKey());
			String value = sanitizer.clean(entry.getValue());
			sb.append("# " + key + "=" + value + "\n");
		}
		sb.append("# __PLATFORMLAYER__METADATA__END__\n");

		sb.append("set -e\n");
		for (String command : commands) {
			sb.append(command);
			sb.append("\n");
		}
		return sb.toString();
	}

	public byte[] getBytes() {
		return Utf8.getBytes(toString());
	}

}
