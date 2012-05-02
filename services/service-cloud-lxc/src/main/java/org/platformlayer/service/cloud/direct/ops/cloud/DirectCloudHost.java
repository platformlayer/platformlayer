package org.platformlayer.service.cloud.direct.ops.cloud;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.pool.FilesystemBackedPool;
import org.platformlayer.service.cloud.direct.model.DirectHost;
import org.platformlayer.service.cloud.direct.ops.DirectCloudUtils;

public class DirectCloudHost {
	// private static final File MACHINE_CONF_DIR = new File("/var/lib/lxc");

	final OpsTarget host;

	// final PropertiesFileStore machineStore;

	final FilesystemBackedPool addressPool;
	final DirectHost model;

	public DirectCloudHost(DirectHost model, OpsTarget host) {
		this.model = model;
		this.host = host;
		// this.machineStore = new PropertiesFileStore(host, MACHINE_CONF_DIR);

		this.addressPool = DirectCloudUtils.getPrivateAddressPool().get();
	}

	public void terminate(String lxcId) throws OpsException {
		Command command = Command.build("lxc-stop -n {0}", lxcId);
		host.executeCommand(command);
	}

	public void start(String lxcId) throws OpsException {
		Command command = Command.build("lxc-start -n {0} -d", lxcId);
		host.executeCommand(command);
	}

	// public LxcMachineInfo findMachineByLxcId(String lxcId) throws OpsException {
	// Properties properties = machineStore.readProperties(lxcId);
	// Tags tags = machineStore.asTags(properties);
	// if (properties == null)
	// return null;
	// return new LxcMachineInfo(lxcId, tags);
	// }
	//
	// public LxcMachineInfo findMachine(Tag tag) throws OpsException {
	// String lxcId = machineStore.findFirst(Lists.newArrayList(tag));
	// if (lxcId == null)
	// return null;
	// return findMachineByLxcId(lxcId);
	// }

	public OpsTarget getTarget() {
		return host;
	}

	public FilesystemBackedPool getAddressPool() {
		return addressPool;
	}

	// public LxcMachineInfo addMachine(String lxcId, Tags tags) throws OpsException {
	// Properties properties = machineStore.toProperties(tags);
	// machineStore.writeProperties(lxcId, properties);
	//
	// return new LxcMachineInfo(lxcId, tags);
	// }

	public DirectHost getModel() {
		return model;
	}

}
