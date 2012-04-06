package org.platformlayer.keystone.cli.model;

import com.fathomdb.cli.autocomplete.HasAutoCompletor;
import org.platformlayer.keystone.cli.autocomplete.UserNameAutoCompleter;
import org.platformlayer.model.StringWrapper;

@HasAutoCompletor(UserNameAutoCompleter.class)
public class UserName extends StringWrapper {

    public UserName(String key) {
        super(key);
    }
}
