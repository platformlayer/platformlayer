package org.platformlayer.keystone.cli.autocomplete;

import java.util.List;

import org.platformlayer.auth.UserRepository;
import com.fathomdb.cli.CliContext;
import com.fathomdb.cli.autocomplete.SimpleArgumentAutoCompleter;
import org.platformlayer.keystone.cli.KeystoneCliContext;

public class ProjectNameAutoCompleter extends SimpleArgumentAutoCompleter {

    @Override
    public List<String> doComplete(CliContext context, String prefix) throws Exception {
        if (prefix.length() <= 2) {
            // Let's avoid returning thousands of projects
            return null;
        }

        KeystoneCliContext keystoneContext = (KeystoneCliContext) context;
        UserRepository userRepository = keystoneContext.getUserRepository();
        List<String> userIds = userRepository.listAllProjectNames(prefix);
        addSuffix(userIds, " ");

        return userIds;
    }

}
