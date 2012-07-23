package org.platformlayer.client.cli.model;

import com.fathomdb.cli.StringWrapper;
import com.fathomdb.cli.autocomplete.AutoCompleteAction;
import com.fathomdb.cli.autocomplete.HasAutoCompletor;

@HasAutoCompletor(AutoCompleteAction.class)
public class CliAction extends StringWrapper {
	public CliAction(String key) {
		super(key);
	}

}
