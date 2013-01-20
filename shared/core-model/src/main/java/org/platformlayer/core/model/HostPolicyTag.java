package org.platformlayer.core.model;

import java.util.List;

import com.google.common.collect.Lists;

public class HostPolicyTag {
	public static final String TRUSTED_HOST = "host.trusted";

	public static Tag requireTrusted() {
		return Tag.HOST_POLICY.build(TRUSTED_HOST);
	}

	public static List<String> satisfy(HostPolicy hostPolicy, Tags cloudTags) {
		List<String> unsatisified = Lists.newArrayList();

		if (hostPolicy != null) {
			List<String> policies = hostPolicy.getPolicies();
			List<String> have = Tag.HOST_POLICY.find(cloudTags);
			for (String policy : policies) {
				if (have.contains(policy)) {
					continue;
				}

				unsatisified.add(policy);
			}
		}

		return unsatisified;
	}
}
