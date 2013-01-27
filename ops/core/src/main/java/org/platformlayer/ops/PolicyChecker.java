package org.platformlayer.ops;

import java.util.List;

import org.platformlayer.core.model.HostPolicy;
import org.platformlayer.core.model.HostPolicyTag;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class PolicyChecker {
	private static final Logger log = LoggerFactory.getLogger(PolicyChecker.class);

	// By default we require 99.9% uptime (1 hour per month) unless otherwise specified
	public float defaultUptime = 99.9f;

	public static Tag makeDatacenterUptime(float percent) {
		return Tag.HOST_POLICY.build(HostPolicyTag.DATACENTER_UPTIME_PREFIX + String.valueOf(percent));
	}

	public static Tag requireTrusted() {
		return Tag.HOST_POLICY.build(HostPolicyTag.TRUSTED_HOST);
	}

	public List<String> satisfy(HostPolicy hostPolicy, Tags cloudTags) {
		List<String> unsatisified = Lists.newArrayList();

		boolean checkedUptime = false;

		List<String> haves = Tag.HOST_POLICY.find(cloudTags);

		if (hostPolicy != null) {
			List<String> policies = hostPolicy.getPolicies();
			for (String policy : policies) {
				if (policy.startsWith(HostPolicyTag.DATACENTER_UPTIME_PREFIX)) {
					checkedUptime = true;
				}

				if (haves.contains(policy)) {
					continue;
				}

				boolean satisfied = false;

				if (policy.startsWith(HostPolicyTag.DATACENTER_UPTIME_PREFIX)) {
					Float wantUptime = toUptime(policy);

					if (wantUptime != null) {
						if (hasUptime(haves, wantUptime)) {
							satisfied = true;
						}
					}
				}

				if (!satisfied) {
					unsatisified.add(policy);
				}
			}
		}

		if (!checkedUptime) {
			if (!hasUptime(haves, defaultUptime)) {
				unsatisified.add(makeDatacenterUptime(defaultUptime).getValue());
			}
		}

		return unsatisified;
	}

	private boolean hasUptime(List<String> haves, float wantUptime) {
		// Accept better uptime
		for (String have : haves) {
			if (have.startsWith(HostPolicyTag.DATACENTER_UPTIME_PREFIX)) {
				Float haveUptime = toUptime(have);

				if (haveUptime != null) {
					if (haveUptime >= wantUptime) {
						return true;
					}
				}
			}
		}

		return false;

	}

	private Float toUptime(String policy) {
		if (!policy.startsWith(HostPolicyTag.DATACENTER_UPTIME_PREFIX)) {
			log.warn("Unexpected policy prefix parsing uptime: " + policy);
			return null;
		}

		String suffix = policy.substring(HostPolicyTag.DATACENTER_UPTIME_PREFIX.length());
		Float f = Float.parseFloat(suffix);
		return f;
	}

	public static boolean isSatisfied(HostPolicy hostPolicy, Tags tags) {
		PolicyChecker checker = new PolicyChecker();

		List<String> unsatisfied = checker.satisfy(hostPolicy, tags);
		if (!unsatisfied.isEmpty()) {
			log.info("Cannot satisfy policy requirements: " + Joiner.on(",").join(unsatisfied));
			return false;
		}

		return true;
	}
}
