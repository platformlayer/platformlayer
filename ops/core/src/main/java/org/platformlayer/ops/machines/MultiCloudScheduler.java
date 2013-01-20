package org.platformlayer.ops.machines;

import org.platformlayer.ops.MachineCreationRequest;
import org.platformlayer.ops.OpsException;

import com.google.inject.ImplementedBy;

@ImplementedBy(SimpleMultiCloudScheduler.class)
public interface MultiCloudScheduler {
	MachineProvider pickCloud(MachineCreationRequest request) throws OpsException;

	// List<MachineProvider> listClouds() throws OpsException;
}
