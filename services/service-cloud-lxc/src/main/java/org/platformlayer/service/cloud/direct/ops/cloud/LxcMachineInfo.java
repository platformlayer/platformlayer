package org.platformlayer.service.cloud.direct.ops.cloud;

import org.platformlayer.core.model.Tags;

public class LxcMachineInfo {
	public final String lxcId;
	public final Tags tags;

	public LxcMachineInfo(String lxcId, Tags tags) {
		super();
		this.lxcId = lxcId;
		this.tags = tags;
	}

	public Tags getTags() {
		return tags;
	}

}
