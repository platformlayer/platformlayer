package org.platformlayer.ops.users;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PosixGroupManagement {
	public static class Group {
		public String name;
		public int id;
		public List<String> members;
	}

	public static Map<String, Group> getGroups(OpsTarget target) throws OpsException {
		String groupFile = target.readTextFile(new File("/etc/group"));

		Map<String, Group> groups = Maps.newHashMap();

		for (String line : groupFile.split("\n")) {
			line = line.trim();

			// Ignore comments
			if (line.startsWith("#")) {
				continue;
			}

			List<String> tokens = Lists.newArrayList(Splitter.on(':').split(line));

			if (tokens.size() != 4) {
				throw new OpsException("Error parsing groups line: " + line);
			}

			Group group = new Group();
			group.name = tokens.get(0);
			// tokens[1] is password, but normally 'x'
			group.id = Integer.parseInt(tokens.get(2));
			group.members = Lists.newArrayList(tokens.get(3).split(","));
			groups.put(group.name, group);
		}

		return groups;
	}

	public static class User {
		final List<String> tokens;

		public User(List<String> tokens) {
			this.tokens = tokens;
		}

	}

	public static Map<String, User> readUsers(OpsTarget target) throws OpsException {
		File passwdFile = new File("/etc/passwd");
		String passwd = target.readTextFile(passwdFile);

		Map<String, User> users = Maps.newHashMap();
		for (String line : Splitter.on("\n").split(passwd)) {
			line = line.trim();

			// Ignore comments
			if (line.startsWith("#"))
				continue;

			List<String> tokens = Lists.newArrayList(Splitter.on(':').split(line));
			if (tokens.isEmpty())
				continue;

			String name = tokens.get(0);

			User user = new User(tokens);
			users.put(name, user);
		}

		return users;
	}
}
