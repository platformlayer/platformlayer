package org.platformlayer.ops;

import java.io.File;
import java.net.Inet4Address;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Provider;

import org.platformlayer.TimeSpan;
import org.platformlayer.core.model.Secret;

import com.google.common.collect.Lists;

public class Command {
	public static class Argument {
		public Argument(String simple) {
			this(simple, simple);
		}

		public Argument(String command, String masked) {
			this.command = command;
			this.masked = masked;
		}

		String command;
		String masked;

		public void replace(String key, Argument arg) {
			if (command.contains(key)) {
				command = command.replace(key, arg.command);
				masked = masked.replace(key, arg.masked);
			}
		}

		public Argument setMasked(String masked) {
			this.masked = masked;
			return this;
		}

		public static Argument buildQuoted(String literalPrefix, String value) {
			Argument arg = new Argument(literalPrefix + escapeQuoted(value));
			return arg;
		}

		public static Argument buildLiteral(String literal) {
			Argument arg = new Argument(literal);
			return arg;
		}
	}

	public static final String MASKED = "***";

	final List<Argument> args;

	private TimeSpan timeout = TimeSpan.TWO_MINUTES;
	private CommandEnvironment env;

	private Command(String executable) {
		this();
		addLiteral(escapeFile(executable));
	}

	private Command() {
		args = Lists.newArrayList();
	}

	private Command(List<Argument> args) {
		this.args = args;
	}

	public Command addLiteral(String literalArg) {
		addUnmaskedArg(literalArg);
		return this;
	}

	static String escapeFile(File fileArg) {
		String path = fileArg.getPath();
		return escapeFile(path);
	}

	static String escapeFile(String path) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < path.length(); i++) {
			char c = path.charAt(i);
			if (Character.isLetterOrDigit(c)) {
				sb.append(c);
			} else {
				switch (c) {
				case '.':
				case '-':
				case '_':
				case '/':
				case ':':
					sb.append(c);
					break;

				default:
					throw new IllegalArgumentException("Don't know how to escape character: " + ((int) c));
				}
			}
		}

		return sb.toString();
	}

	public void addFile(String literalPrefix, File fileArg) {
		addUnmaskedArg(literalPrefix + escapeFile(fileArg));
	}

	private Argument addUnmaskedArg(String literalArg) {
		Argument arg = Argument.buildLiteral(literalArg);
		args.add(arg);
		return arg;
	}

	public void addFile(File fileArg) {
		addUnmaskedArg(escapeFile(fileArg));
	}

	static String escapeQuoted(String s) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (Character.isLetterOrDigit(c)) {
				sb.append(c);
			} else {
				switch (c) {
				case ' ':
				case '-':
				case '_':
				case '.':
				case ',':
				case ':':
				case '/':
				case '%':
				case '{':
				case '}':
				case '@':
				case '=':
				case '\'':
				case ';':
				case '<':
				case '>':
				case '(':
				case ')':
				case '*':
				case '?':
				case '&':
				case '+':
				case '[':
				case ']':
					sb.append(c);
					break;

				case '\\':
					sb.append("\\\\");
					break;
				case '\"':
					sb.append("\\\"");
					break;

				case '$':
					sb.append("\\$");
					break;

				default:
					throw new IllegalArgumentException("Don't know how to escape character: " + c);
				}
			}
		}

		return "\"" + sb.toString() + "\"";
	}

	public void addQuoted(String literalPrefix, String value) {
		addArgument(Argument.buildQuoted(literalPrefix, value));
	}

	public void addArgument(Argument arg) {
		args.add(arg);
	}

	public void addQuoted(String literalPrefix, Secret value) {
		String plaintext = value.plaintext();

		String command = literalPrefix + escapeQuoted(plaintext);
		String masked = literalPrefix + MASKED;

		args.add(new Argument(command, masked));
	}

	public void addQuoted(String value) {
		addUnmaskedArg(escapeQuoted(value));
	}

	public void addQuoted(Secret value) {
		String command = value.plaintext();
		String masked = MASKED;
		args.add(new Argument(command, masked));
	}

	public static Command build(String literal, Object... arguments) {
		List<Argument> tokens = Lists.newArrayList();
		for (String token : literal.split(" ")) {
			Argument arg = new Argument(token);
			tokens.add(arg);
		}

		for (int i = 0; i < arguments.length; i++) {
			Argument arg = escape(arguments[i]);

			for (int j = 0; j < tokens.size(); j++) {
				String key = "{" + i + "}";
				tokens.get(j).replace(key, arg);
			}
		}

		Command command = new Command(tokens);

		return command;
	}

	private static Argument escape(Object arg) {
		String command = null;
		String masked = null;
		if (arg instanceof Provider) {
			Provider<?> provider = (Provider<?>) arg;
			return escape(provider.get());
		} else if (arg instanceof File) {
			command = escapeFile((File) arg);
		} else if (arg instanceof Secret) {
			String plaintext = ((Secret) arg).plaintext();
			command = escapeQuoted(plaintext);
			masked = MASKED;
		} else if (arg instanceof String) {
			command = escapeQuoted((String) arg);
		} else if (arg instanceof Inet4Address) {
			command = escapeQuoted(((Inet4Address) arg).getHostAddress());
		} else {
			throw new IllegalArgumentException("Don't know how to handle argument of type " + arg.getClass());
		}
		if (masked == null) {
			masked = command;
		}
		return new Argument(command, masked);

	}

	// public Iterable<String> getCommandArgs() {
	// return commandArgs;
	// }

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < args.size(); i++) {
			Argument arg = args.get(i);
			if (i != 0) {
				sb.append(" ");
			}
			sb.append(arg.masked);
		}
		return sb.toString();
	}

	public TimeSpan getTimeout() {
		return timeout;
	}

	public Command setTimeout(TimeSpan timeout) {
		this.timeout = timeout;
		return this;
	}

	public Command setEnvironment(CommandEnvironment env) {
		this.env = env;
		return this;
	}

	public CommandEnvironment getEnvironment() {
		return env;
	}

	public String buildCommandString() {
		StringBuilder sb = new StringBuilder();
		if (env != null) {
			for (Entry<String, String> e : env.all()) {
				sb.append(e.getKey() + "=" + escapeQuoted(e.getValue()) + " ");
			}
		}

		for (int i = 0; i < args.size(); i++) {
			Argument arg = args.get(i);
			if (i != 0) {
				sb.append(" ");
			}
			sb.append(arg.command);
		}

		return sb.toString();
	}

	public Command prefix(Object... prefixArgs) {
		List<Argument> newArgs = Lists.newArrayList();
		for (Object prefixArg : prefixArgs) {
			Argument arg = escape(prefixArg);
			newArgs.add(arg);
		}
		newArgs.addAll(this.args);
		Command newCommand = new Command(newArgs);
		newCommand.timeout = this.timeout;
		if (this.env != null) {
			newCommand.env = this.env.deepCopy();
		}
		return newCommand;
	}

	public Command pipeTo(Command r) {
		Command newCommand = new Command(this.args);
		newCommand.timeout = this.timeout;
		if (this.env != null) {
			newCommand.env = this.env.deepCopy();
		}

		newCommand.addUnmaskedArg("|");
		newCommand.args.addAll(r.args);

		return newCommand;
	}
}
