package org.platformlayer.keystone.cli.model;

import org.platformlayer.keystone.cli.autocomplete.UserNameAutoCompleter;
import org.platformlayer.model.StringWrapper;

import com.fathomdb.cli.autocomplete.HasAutoCompletor;

@HasAutoCompletor(UserNameAutoCompleter.class)
public class UserName extends StringWrapper {

	public UserName(String key) {
		super(key);
	}
}
