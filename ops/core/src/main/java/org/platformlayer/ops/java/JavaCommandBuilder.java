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

	final List<File> classpathFolders = Lists.newArrayList();
	final List<Argument> arguments = Lists.newArrayList();

	public void addArgument(Argument argument) {
		arguments.add(argument);
	}

	public void addClasspathFolder(File f) {
		classpathFolders.add(f);
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

		StringBuilder classpath = new StringBuilder();
		for (File classpathFolder : classpathFolders) {
			if (classpath.length() != 0) {
				classpath.append(":");
			}
			// TODO: Adding * is gross
			classpath.append(classpathFolder.getAbsolutePath() + "/*");
		}

		// TODO: This is not nice either
		classpath.append(":.");

		command.addLiteral("-cp").addQuoted(classpath.toString());

		command.addQuoted(mainClass);

		for (Argument argument : arguments) {
			command.addArgument(argument);
		}

		return command;
	}
}
