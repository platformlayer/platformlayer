package org.platformlayer.service.cloud.openstack.ops;

import javax.inject.Inject;

import org.openstack.client.OpenstackException;
import org.openstack.client.common.OpenstackComputeClient;
import org.openstack.model.compute.CreateSecurityGroupRuleRequest;
import org.openstack.model.compute.SecurityGroup;
import org.openstack.model.compute.SecurityGroupRule;
import org.openstack.model.compute.Server;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.service.cloud.openstack.model.OpenstackCloud;
import org.platformlayer.service.cloud.openstack.model.OpenstackPublicEndpoint;
import org.platformlayer.service.cloud.openstack.ops.openstack.CloudBehaviours;
import org.platformlayer.service.cloud.openstack.ops.openstack.OpenstackCloudContext;
import org.platformlayer.service.cloud.openstack.ops.openstack.OpenstackCloudHelpers;
import org.platformlayer.service.cloud.openstack.ops.openstack.OpenstackComputeMachine;

import com.google.common.base.Objects;

public class EnsureFirewallIngress {
	public OpenstackPublicEndpoint model;

	@Inject
	OpenstackCloudContext cloudContext;

	// Set by handler
	String publicAddress;

	@Inject
	OpenstackCloudHelpers openstackHelpers;

	@Handler
	public void handler(OpenstackCloud cloud, OpenstackComputeMachine machine) throws OpsException, OpenstackException {
		CloudBehaviours cloudBehaviours = new CloudBehaviours(cloud);

		OpenstackComputeClient openstackComputeClient = cloudContext.getComputeClient(cloud);

		// Find the public address, although the OpenStack firewall may be blocking it
		String network = null; // model.network;
		publicAddress = machine.getBestAddress(NetworkPoint.forNetwork(network), model.backendPort);

		if (cloudBehaviours.supportsSecurityGroups()) {
			Server server = machine.getServer();
			SecurityGroup securityGroup = openstackHelpers.getMachineSecurityGroup(openstackComputeClient, server);

			securityGroup = openstackComputeClient.root().securityGroups().securityGroup(securityGroup.getId()).show();

			SecurityGroupRule matchingRule = findMatchingRule(securityGroup);

			if (OpsContext.isConfigure()) {
				if (matchingRule == null) {
					CreateSecurityGroupRuleRequest rule = new CreateSecurityGroupRuleRequest();
					rule.setCidr("0.0.0.0/0");
					rule.setIpProtocol("tcp");
					rule.setFromPort(model.publicPort);
					rule.setToPort(model.publicPort);
					rule.setParentGroupId(securityGroup.getId());

					openstackComputeClient.root().securityGroupRules().create(rule);
				}
			}

			if (OpsContext.isDelete()) {
				if (matchingRule != null) {
					openstackComputeClient.root().securityGroupRules().securityGroupRule(matchingRule.id).delete();
				}
			}
		}
	}

	private SecurityGroupRule findMatchingRule(SecurityGroup securityGroup) {
		for (SecurityGroupRule rule : securityGroup.getRules()) {
			if (!Objects.equal("tcp", rule.ipProtocol)) {
				continue;
			}
			if (!Objects.equal(model.publicPort, rule.fromPort)) {
				continue;
			}
			if (!Objects.equal(model.publicPort, rule.toPort)) {
				continue;
			}

			if (rule.ipRange == null) {
				continue;
			}

			if (!Objects.equal(rule.ipRange.cidr, "0.0.0.0/0")) {
				continue;
			}

			return rule;
		}
		return null;
	}

	public String getPublicAddress() {
		return publicAddress;
	}
}
