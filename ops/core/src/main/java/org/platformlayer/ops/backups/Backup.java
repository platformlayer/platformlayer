package org.platformlayer.ops.backups;

import java.util.Map;

import org.platformlayer.ops.OpsTarget;

import com.google.common.collect.Maps;

public class Backup {
	public OpsTarget target;
	public String objectName;
	public Map<String, String> objectProperties = Maps.newHashMap();
}