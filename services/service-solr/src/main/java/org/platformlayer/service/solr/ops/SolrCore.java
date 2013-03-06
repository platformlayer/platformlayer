package org.platformlayer.service.solr.ops;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.service.solr.ops.SolrCoreHelpers.SolrCoreStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.TimeSpan;
import com.google.common.base.Objects;

public class SolrCore {

	private static final Logger log = LoggerFactory.getLogger(SolrCore.class);

	public String key;

	@Handler
	public void handler(OpsTarget target) throws OpsException {
		SolrCoreHelpers helper = new SolrCoreHelpers(target, key);

		if (OpsContext.isConfigure()) {
			// TODO: only reload if changed??
			SolrCoreStatus status0 = helper.getStatus();

			helper.reload();

			// TODO: It looks like reload is async; hopefully this check deals with that
			SolrCoreStatus status1 = helper.getStatus();

			while (true) {
				String startTime0 = status0.getStartTime();
				String startTime1 = status1.getStartTime();
				if (!Objects.equal(startTime0, startTime1)) {
					break;
				}

				OpsSystem.safeSleep(TimeSpan.ONE_SECOND);
			}
		}
	}

}
