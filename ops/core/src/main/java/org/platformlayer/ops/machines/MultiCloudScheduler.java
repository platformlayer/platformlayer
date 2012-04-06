package org.platformlayer.ops.machines;

import java.util.List;

import org.platformlayer.core.model.MachineCloudBase;
import org.platformlayer.ops.MachineCreationRequest;
import org.platformlayer.ops.OpsException;

import com.google.inject.ImplementedBy;

@ImplementedBy(SimpleMultiCloudScheduler.class)
public interface MultiCloudScheduler {
    MachineCloudBase pickCloud(MachineCreationRequest request) throws OpsException;

    List<MachineCloudBase> listClouds() throws OpsException;
}
