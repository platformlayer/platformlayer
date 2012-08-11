package org.platformlayer.ops;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;

public class CommandEnvironment {
	final Map<String, String> vars = Maps.newHashMap();

	public Set<Entry<String, String>> all() {
		return vars.entrySet();
	}

	public void put(String key, String value) {
		vars.put(key, value);
	}

	public CommandEnvironment deepCopy() {
		CommandEnvironment copy = new CommandEnvironment();
		copy.vars.putAll(this.vars);
		return copy;
	}

	public Set<String> keys() {
		return vars.keySet();
	}

	@Override
	public CommandEnvironment clone() {
		return deepCopy();
	}

	public Map<String, String> asMap() {
		return Collections.unmodifiableMap(vars);
	}

	public static CommandEnvironment build(Map<String, String> env) {
		CommandEnvironment commandEnvironment = new CommandEnvironment();
		commandEnvironment.vars.putAll(env);
		return commandEnvironment;
	}
}
