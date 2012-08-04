package org.platformlayer.service.cloud.direct.ops;

import java.util.Map;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Bound;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.networks.IpRange;
import org.platformlayer.ops.templates.TemplateDataSource;
import org.platformlayer.service.cloud.direct.model.DirectHost;

public class HostTemplate implements TemplateDataSource {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(HostTemplate.class);

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
