package org.platformlayer.keystone.cli.formatters;

import com.fathomdb.cli.formatter.FormatterRegistryBase;

public class KeystoneFormatterRegistry extends FormatterRegistryBase {
	public KeystoneFormatterRegistry() {
		addDefaultFormatters();

		discoverFormatters();
	}

}
