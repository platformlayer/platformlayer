package org.platformlayer.client.cli.model;

import com.fathomdb.cli.StringWrapper;
import com.fathomdb.cli.autocomplete.AutoCompleteAction;
import com.fathomdb.cli.autocomplete.HasAutoCompletor;

@HasAutoCompletor(AutoCompleteAction.class)
public class ConfigureAction extends StringWrapper {
	public ConfigureAction(String key) {
		super(key);
	}

}
