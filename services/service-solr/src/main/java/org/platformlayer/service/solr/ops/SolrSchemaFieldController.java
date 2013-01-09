package org.platformlayer.service.solr.ops;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrSchemaFieldController extends OpsTreeBase {

	private static final Logger log = LoggerFactory.getLogger(SolrSchemaFieldController.class);

	@Handler
	public void handler() throws OpsException {
	}

	@Override
	protected void addChildren() throws OpsException {

	}
}
