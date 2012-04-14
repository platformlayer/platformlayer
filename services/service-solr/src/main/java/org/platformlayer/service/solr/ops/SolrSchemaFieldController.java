package org.platformlayer.service.solr.ops;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.tree.OpsTreeBase;

public class SolrSchemaFieldController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(SolrSchemaFieldController.class);

	@Handler
	public void handler() throws OpsException {
	}

	@Override
	protected void addChildren() throws OpsException {

	}
}
