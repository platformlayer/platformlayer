package org.platformlayer.core.model;

public class HostPolicyTag {
	public static final String TRUSTED_HOST = "host.trusted";

	public static Tag requireTrusted() {
		return Tag.HOST_POLICY.build(TRUSTED_HOST);
	}

	public static final String DATACENTER_UPTIME_PREFIX = "datacenter.uptime:";

	// Annual _datacenter_ downtime 4 days
	public static final String DATACENTER_UPTIME_99 = DATACENTER_UPTIME_PREFIX + "99";

	// Annual _datacenter_ downtime 10 hours
	public static final String DATACENTER_UPTIME_999 = DATACENTER_UPTIME_PREFIX + "99.9";

	// Annual _datacenter_ downtime 1 hour
	public static final String DATACENTER_UPTIME_9999 = DATACENTER_UPTIME_PREFIX + "99.99";

	// Annual _datacenter_ downtime 5 minutes
	public static final String DATACENTER_UPTIME_99999 = DATACENTER_UPTIME_PREFIX + "99.999";

	public static Tag makeDatacenterUptime(float percent) {
		return Tag.HOST_POLICY.build(DATACENTER_UPTIME_PREFIX + String.valueOf(percent));
	}
}
