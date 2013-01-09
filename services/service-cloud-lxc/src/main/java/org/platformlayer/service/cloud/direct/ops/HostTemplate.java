package org.platformlayer.service.cloud.direct.ops;

import java.util.Map;

import org.platformlayer.ops.Bound;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.networks.IpRange;
import org.platformlayer.ops.templates.TemplateDataSource;
import org.platformlayer.service.cloud.direct.model.DirectHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostTemplate implements TemplateDataSource {

	private static final Logger log = LoggerFactory.getLogger(HostTemplate.class);

	@Bound
	DirectHost model;

	public String getBridgeInterface() {
		return model.bridge;
	}

	public String getPublicInterface() {
		return model.publicInterface;
	}

	public IpRange getIpv4Public() {
		return IpRange.parse(model.ipv4Public);
	}

	@Override
	public void buildTemplateModel(Map<String, Object> model) throws OpsException {
		model.put("bridgeInterface", getBridgeInterface());
		model.put("publicInterface", getPublicInterface());
	}
}
