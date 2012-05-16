package org.platformlayer.ops.bootstrap;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.SimpleFile;
import org.platformlayer.ops.tree.OpsTreeBase;

public class BootstrapLocales extends OpsTreeBase {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(BootstrapLocales.class);

	@Handler
	public void handler() {

	}

	@Override
	protected void addChildren() throws OpsException {
		addChild(SimpleFile.build(getClass(), RegenerateLocales.LOCALE_GEN_FILE));
		addChild(RegenerateLocales.class);
	}
}
