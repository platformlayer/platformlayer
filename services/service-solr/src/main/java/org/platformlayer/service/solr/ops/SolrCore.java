package org.platformlayer.service.solr.ops;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.CurlRequest;
import org.platformlayer.ops.helpers.CurlResult;

public class SolrCore {
	static final Logger log = Logger.getLogger(SolrCore.class);

	public String key;

	@Handler
	public void handler(OpsTarget target) throws OpsException {
		String url = "http://127.0.0.1:8080/solr/admin/cores?core=" + key;

		if (OpsContext.isConfigure()) {
			// TODO: only if changed??
			url += "&action=RELOAD";

			CurlRequest request = new CurlRequest(url);
			CurlResult result = request.executeRequest(target);
			log.info("result: " + result);
		}
	}
}
