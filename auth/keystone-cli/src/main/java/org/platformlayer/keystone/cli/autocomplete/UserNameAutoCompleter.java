package org.platformlayer.keystone.cli.autocomplete;

import java.util.List;

import org.platformlayer.auth.UserRepository;
import org.platformlayer.keystone.cli.KeystoneCliContext;

import com.fathomdb.cli.CliContext;
import com.fathomdb.cli.autocomplete.SimpleArgumentAutoCompleter;

public class UserNameAutoCompleter extends SimpleArgumentAutoCompleter {

	@Override
	public List<String> doComplete(CliContext context, String prefix) throws Exception {
		if (prefix.length() < 3) {
			// Let's avoid returning thousands of users
			return null;
		}

		KeystoneCliContext keystoneContext = (KeystoneCliContext) context;
		UserRepository userRepository = keystoneContext.getUserRepository();
		List<String> userIds = userRepository.listAllUserNames(prefix);
		addSuffix(userIds, " ");

		return userIds;
	}

}
