package org.platformlayer.service.cloud.direct.ops.lxc;

import java.io.File;
import java.util.Map;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.supervisor.SupervisorProcessConfig;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.direct.ops.kvm.ManagedSupervisorInstance;

public class ManagedLxcInstance extends OpsTreeBase {
	public String id;
	public File base;

	File getRootPath() {
		return base;
	}

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		addChild(buildSupervisorInstance());
	}

	private ManagedSupervisorInstance buildSupervisorInstance() {
		String key = "lxc-" + id;

		Command command = Command.build("lxc-start");

		command.addLiteral("--name").addQuoted(id);

		SupervisorProcessConfig sup = new SupervisorProcessConfig(key);
		Map<String, String> properties = sup.getProperties();
		properties.put("command", command.buildCommandString());

		ManagedSupervisorInstance instance = injected(ManagedSupervisorInstance.class);
		instance.supervisorProcess = sup;
		return instance;
	}
}
