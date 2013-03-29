package org.platformlayer.service.cloud.google.ops;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.service.cloud.google.model.GoogleCloud;
import org.platformlayer.service.cloud.google.model.GoogleCloudPublicEndpoint;
import org.platformlayer.service.cloud.google.ops.compute.GoogleComputeClient;
import org.platformlayer.service.cloud.google.ops.compute.GoogleComputeClientFactory;
import org.platformlayer.service.cloud.google.ops.compute.GoogleComputeMachine;

import com.google.api.services.compute.model.Firewall;
import com.google.api.services.compute.model.Firewall.Allowed;
import com.google.common.base.Objects;

public class EnsureFirewallIngress {
	public GoogleCloudPublicEndpoint model;

	// Set by handler
	String publicAddress;

	@Inject
	GoogleComputeClientFactory googleComputeClientFactory;

	@Handler
	public void handler(GoogleCloud cloud, GoogleComputeMachine machine) throws OpsException {
		GoogleComputeClient client = googleComputeClientFactory.getComputeClient(cloud);

		// Find the public address, although the Google Cloud firewall may be blocking it
		publicAddress = machine.getNetworkPoint().getBestAddress(NetworkPoint.forPublicInternet());

		String serverLink = machine.getServerSelfLink();
		List<Firewall> rules = client.getInstanceFirewallRules(serverLink);

		Firewall matchingRule = findMatchingRule(rules);

		if (OpsContext.isConfigure()) {
			if (matchingRule == null) {
				Firewall rule = new Firewall();
				rule.setSourceRanges(Arrays.asList("0.0.0.0/0"));
				rule.setName("pl-" + UUID.randomUUID().toString());

				Allowed allowed = new Allowed();
				allowed.setIPProtocol("tcp");
				allowed.setPorts(Arrays.asList("" + model.publicPort));
				rule.setAllowed(Arrays.asList(allowed));

				rule.setNetwork(client.buildNetworkUrl("default"));

				client.createFirewallRule(rule);
			}
		}

		if (OpsContext.isDelete()) {
			if (matchingRule != null) {
				client.deleteFirewallRule(matchingRule);
			}
		}
	}

	private Firewall findMatchingRule(List<Firewall> rules) {
		for (Firewall rule : rules) {
			List<Allowed> allowedList = rule.getAllowed();

			boolean matchesPortAndProtocol = false;

			if (allowedList != null) {
				for (Allowed allowed : allowedList) {
					if (!Objects.equal("tcp", allowed.getIPProtocol())) {
						continue;
					}

					List<String> ports = allowed.getPorts();
					if (ports != null) {
						for (String port : ports) {
							if (port.contains("-")) {
								if (port.equals(model.publicPort + "-" + model.publicPort)) {
									matchesPortAndProtocol = true;
								}
							} else {
								if (port.equals(model.publicPort + "")) {
									matchesPortAndProtocol = true;
								}
							}
						}
					}
				}
			}

			if (!matchesPortAndProtocol) {
				continue;
			}

			boolean matchedSourceRange = false;

			List<String> sourceRanges = rule.getSourceRanges();
			if (sourceRanges == null) {
				if (rule.getSourceTags() == null) {
					matchedSourceRange = true;
				}
			} else {
				for (String sourceRange : sourceRanges) {
					if (Objects.equal(sourceRange, "0.0.0.0/0")) {
						matchedSourceRange = true;
					}
				}
			}

			if (matchedSourceRange) {
				return rule;
			}
		}
		return null;
	}

	public String getPublicAddress() {
		return publicAddress;
	}
}
