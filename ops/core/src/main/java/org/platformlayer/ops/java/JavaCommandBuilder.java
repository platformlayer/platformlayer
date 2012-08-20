package org.platformlayer.ops.java;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Command.Argument;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class JavaCommandBuilder {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(JavaCommandBuilder.class);

	final Map<String, String> defines = Maps.newHashMap();
	String mainClass;

	static class ClasspathEntry {
		public File path;
		public boolean wildcard;
	}

	final List<ClasspathEntry> classpath = Lists.newArrayList();
	final List<Argument> arguments = Lists.newArrayList();

	public void addArgument(Argument argument) {
		arguments.add(argument);
	}

	public void addClasspath(File path, boolean wildcard) {
		ClasspathEntry entry = new ClasspathEntry();
		entry.path = path;
		entry.wildcard = wildcard;
		classpath.add(entry);
	}

	public void addClasspathFolder(File path) {
		addClasspath(path, true);
	}

	public void addDefine(String key, String value) {
		defines.put(key, value);
	}

	public void addDefine(String key, File value) {
		addDefine(key, value.getAbsolutePath());
	}

	public String getMainClass() {
		return mainClass;
	}

	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}

	public Command get() {
		Command command = Command.build("java");
		command.addLiteral("-server");

		for (Map.Entry<String, String> define : defines.entrySet()) {
			command.addLiteral("-D" + define.getKey() + "=" + define.getValue());
		}

		StringBuilder cp = new StringBuilder();
		for (ClasspathEntry entry : classpath) {
			if (cp.length() != 0) {
				cp.append(":");
			}
			String s = entry.path.getAbsolutePath();
			if (entry.wildcard) {
				// TODO: Adding * is gross
				s += "/*";
			}
			cp.append(s);
		}

		// TODO: This is not nice either
		cp.append(":.");

		command.addLiteral("-cp").addQuoted(cp.toString());

		command.addQuoted(mainClass);

		for (Argument argument : arguments) {
			command.addArgument(argument);
		}

		return command;
	}
}
