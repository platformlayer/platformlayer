package org.platformlayer.keystone.cli.autocomplete;

import java.util.List;

import org.platformlayer.auth.UserDatabase;
import org.platformlayer.keystone.cli.KeystoneCliContext;

import com.fathomdb.cli.CliContext;
import com.fathomdb.cli.autocomplete.SimpleArgumentAutoCompleter;

public class ProjectNameAutoCompleter extends SimpleArgumentAutoCompleter {

	@Override
	public List<String> doComplete(CliContext context, String prefix) throws Exception {
		if (prefix.length() <= 2) {
			// Let's avoid returning thousands of projects
			return null;
		}

		KeystoneCliContext keystoneContext = (KeystoneCliContext) context;
		UserDatabase userRepository = keystoneContext.getUserRepository();
		List<String> userIds = userRepository.listAllProjectNames(prefix);
		addSuffix(userIds, " ");

		return userIds;
	}

}
