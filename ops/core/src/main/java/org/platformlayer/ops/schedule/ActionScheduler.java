package org.platformlayer.ops.schedule;

import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.PlatformLayerEndpointInfo;
import org.platformlayer.auth.Authenticator;
import org.platformlayer.auth.DirectAuthenticationToken;
import org.platformlayer.auth.DirectAuthenticator;
import org.platformlayer.core.model.Action;
import org.platformlayer.core.model.JobSchedule;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.OpsException;

import com.fathomdb.crypto.FathomdbCrypto;
import com.fathomdb.utils.Hex;
import com.google.common.base.Joiner;

public class ActionScheduler {
	private static final Logger log = Logger.getLogger(ActionScheduler.class);

	@Inject
	Scheduler scheduler;

	public void putJob(String key, final PlatformLayerEndpointInfo endpoint, JobSchedule schedule,
			final PlatformLayerKey target, final Action action) throws OpsException {
		// We could create a "per-job" "single-purpose" key??

		SchedulerRecord record = new SchedulerRecord();
		record.key = key;
		record.schedule = schedule;
		ActionTask task = new ActionTask();
		record.task = task;

		task.endpoint = map(endpoint);
		task.target = target;
		task.action = action;

		scheduler.putJob(record);
	}

	private EndpointRecord map(PlatformLayerEndpointInfo in) {
		EndpointRecord out = new EndpointRecord();
		out.url = in.getPlatformlayerBaseUrl();
		out.project = in.getProjectId().getKey();
		List<String> trustKeys = in.getTrustKeys();
		if (trustKeys != null && !trustKeys.isEmpty()) {
			out.trustKeys = Joiner.on(",").join(trustKeys);
		}
		Authenticator authenticator = in.getAuthenticator();
		if (authenticator instanceof DirectAuthenticator) {
			DirectAuthenticator directAuthenticator = (DirectAuthenticator) authenticator;
			DirectAuthenticationToken token = directAuthenticator.getAuthenticationToken();
			out.secret = Secret.build(Hex.toHex(FathomdbCrypto.serialize(token.getSecret())));
			out.token = token.getToken();
		} else {
			throw new UnsupportedOperationException();
		}
		return out;
	}

}
