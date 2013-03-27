package org.openstack.keystone.resources.user;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fathomdb.TimeSpan;
import com.fathomdb.ratelimit.RateLimit;
import com.fathomdb.ratelimit.RateLimitSystem;

@Singleton
public class LoginLimits {
	final RateLimit[] FAILED_LOGINS_BY_USERNAME;
	final RateLimit[] FAILED_LOGINS_BY_IP;

	@Inject
	public LoginLimits(RateLimitSystem rateLimitSystem) {
		FAILED_LOGINS_BY_USERNAME = new RateLimit[3];

		FAILED_LOGINS_BY_USERNAME[0] = new RateLimit(rateLimitSystem,
				"loginfail/user/5m/", TimeSpan.FIVE_MINUTES, 10);
		FAILED_LOGINS_BY_USERNAME[1] = new RateLimit(rateLimitSystem,
				"loginfail/user/1h/", TimeSpan.ONE_HOUR, 20);
		FAILED_LOGINS_BY_USERNAME[2] = new RateLimit(rateLimitSystem,
				"loginfail/user/1d/", TimeSpan.ONE_DAY, 30);

		FAILED_LOGINS_BY_IP = new RateLimit[3];

		FAILED_LOGINS_BY_IP[0] = new RateLimit(rateLimitSystem,
				"loginfail/ip/5m/", TimeSpan.FIVE_MINUTES, 10);
		FAILED_LOGINS_BY_IP[1] = new RateLimit(rateLimitSystem,
				"loginfail/ip/1h/", TimeSpan.ONE_HOUR, 20);
		FAILED_LOGINS_BY_IP[2] = new RateLimit(rateLimitSystem,
				"loginfail/ip/1d/", TimeSpan.ONE_DAY, 30);
	}

	public boolean isOverLimit(String ip, String username) {
		if (username != null) {
			for (RateLimit rateLimit : FAILED_LOGINS_BY_USERNAME) {
				if (rateLimit.isOverLimit(username)) {
					return true;
				}
			}
		}

		if (ip != null) {
			for (RateLimit rateLimit : FAILED_LOGINS_BY_IP) {
				if (rateLimit.isOverLimit(ip)) {
					return true;
				}
			}
		}

		return false;
	}

	public void recordFail(String ip, String username) {
		if (username != null) {
			for (RateLimit rateLimit : FAILED_LOGINS_BY_USERNAME) {
				rateLimit.add(username, 1);
			}
		}

		if (ip != null) {
			for (RateLimit rateLimit : FAILED_LOGINS_BY_IP) {
				rateLimit.add(ip, 1);
			}
		}
	}
}