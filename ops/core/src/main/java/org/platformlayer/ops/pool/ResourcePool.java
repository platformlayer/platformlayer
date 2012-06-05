package org.platformlayer.ops.pool;

import java.io.File;
import java.util.Properties;

import org.platformlayer.ops.OpsException;

public interface ResourcePool {

	Properties readProperties(String key) throws OpsException;

	String assign(File owner, boolean required) throws OpsException;

	String findAssigned(File holder) throws OpsException;

	void release(File holder, String key) throws OpsException;

}
