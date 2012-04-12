package org.platformlayer.keystone.cli;

import org.platformlayer.RepositoryException;
import org.platformlayer.auth.OpsUser;
import org.platformlayer.auth.UserRepository;
import org.platformlayer.keystone.cli.commands.KeystoneCommandRegistry;
import org.platformlayer.keystone.cli.formatters.KeystoneFormatterRegistry;
import org.platformlayer.keystone.cli.guice.CliModule;

import com.fathomdb.cli.CliContextBase;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class KeystoneCliContext extends CliContextBase {
    final KeystoneCliOptions options;
    private Injector injector;

    public KeystoneCliContext(KeystoneCommandRegistry commandRegistry, KeystoneCliOptions options) {
        super(commandRegistry, new KeystoneFormatterRegistry());
        this.options = options;
    }

    @Override
    public void connect() throws Exception {
        this.injector = Guice.createInjector(new CliModule(options));
    }

    public UserRepository getUserRepository() {
        return injector.getInstance(UserRepository.class);
    }

    public OpsUser login() throws RepositoryException {
        String username = options.getUsername();
        String password = options.getPassword();
        if (username == null || password == null) {
            throw new IllegalArgumentException("Must specify username & password");
        }
        OpsUser user = getUserRepository().findUser(username);
        if (user != null) {
            if (!user.isPasswordMatch(password)) {
                user = null;
            }
        }
        if (user == null) {
            throw new SecurityException("Credentials were not valid");
        }
        user.unlockWithPassword(password);
        return user;
    }
}
