package org.platformlayer.service.memcache.ops;

import java.util.Map;

import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.templates.TemplateDataSource;

public class MemcacheTemplateModel implements TemplateDataSource {

	@Override
	public void buildTemplateModel(Map<String, Object> model) throws OpsException {
		// TODO: Determine memory size based on instance size
		model.put("memoryMb", "800");
		model.put("bindAddress", "0.0.0.0");
	}

}
