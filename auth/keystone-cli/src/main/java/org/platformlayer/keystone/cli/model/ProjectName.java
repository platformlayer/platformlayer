package org.platformlayer.keystone.cli.model;

import org.platformlayer.keystone.cli.autocomplete.ProjectNameAutoCompleter;
import org.platformlayer.model.StringWrapper;

import com.fathomdb.cli.autocomplete.HasAutoCompletor;

@HasAutoCompletor(ProjectNameAutoCompleter.class)
public class ProjectName extends StringWrapper {

	public ProjectName(String key) {
		super(key);
	}
}
