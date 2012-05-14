package org.platformlayer.service.dns.ops;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.templates.TemplateDataSource;

public class DnsServerTemplateData implements TemplateDataSource {
	static final Logger log = Logger.getLogger(DnsServerTemplateData.class);

	public File getInstanceDir() {
		return DnsHelpers.PATH_BASE;
	}

	@Override
	public void buildTemplateModel(Map<String, Object> model) throws OpsException {
		model.put("instanceDir", getInstanceDir());
		model.put("installDir", getInstallDir());
		model.put("jvmArgs", getJvmArgs());
	}

	private String getJvmArgs() {
		StringBuilder sb = new StringBuilder();
		sb.append("-server ");
		return sb.toString();
	}

	public File getInstallDir() {
		return new File("/opt/dns");
	}

	public File getZonesDir() {
		return DnsHelpers.PATH_ZONES;
	}

}
