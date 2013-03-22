package org.platformlayer.ops.helpers;

import java.util.List;

import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsException;

public interface MachineCluster {
	List<Machine> getMachines(boolean required) throws OpsException;
}
