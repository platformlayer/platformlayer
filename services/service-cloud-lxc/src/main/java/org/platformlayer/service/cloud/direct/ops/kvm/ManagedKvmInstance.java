package org.platformlayer.service.cloud.direct.ops.kvm;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Provider;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.supervisor.ManagedSupervisorInstance;
import org.platformlayer.ops.supervisor.SupervisorProcessConfig;
import org.platformlayer.ops.templates.TemplateDataSource;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.direct.ops.kvm.monitor.KvmConfig.KvmDrive;
import org.platformlayer.service.cloud.direct.ops.kvm.monitor.KvmConfig.KvmNic;

public class ManagedKvmInstance extends OpsTreeBase {
	public String id;
	public File base;

	public boolean disableKvm;

	public int memoryMb = 256;
	public int vcpus = 1;

	public List<KvmNic> nics;
	public Provider<List<KvmDrive>> drives;
	public Provider<Properties> monitor;
	public Provider<Properties> vnc;

	File getRootPath() {
		return base;
	}

	File getDeviceConfigPath() {
		return new File(base, "devices.conf");
	}

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		addChild(TemplatedFile.build(buildDeviceConfigModel(), getDeviceConfigPath()));
		addChild(buildSupervisorInstance());
	}

	private TemplateDataSource buildDeviceConfigModel() {
		return new TemplateDataSource() {
			@Override
			public void buildTemplateModel(Map<String, Object> model) throws OpsException {
				model.put("instance", ManagedKvmInstance.this);

				model.put("nics", nics);
				model.put("drives", drives);
			}
		};
	}

	private ManagedSupervisorInstance buildSupervisorInstance() {
		String key = "kvm-" + id;

		Command command = Command.build("/usr/bin/kvm");

		if (this.vnc != null) {
			Properties vnc = this.vnc.get();
			int port = Integer.parseInt(vnc.getProperty("port"));
			int vncPort = port - 5900;
			command.addLiteral("-vnc");
			if (vnc.contains("address")) {
				command.addQuoted(vnc.getProperty("address") + ":" + vncPort);
			} else {
				command.addQuoted("0.0.0.0:" + vncPort);
			}
		}

		command.addLiteral("-m").addQuoted(Integer.toString(memoryMb));
		command.addLiteral("-smp").addQuoted(Integer.toString(vcpus));
		command.addLiteral("-name").addQuoted(id);
		// command.addLiteral("-nodefconfig");
		command.addLiteral("-nodefaults");
		command.addLiteral("-vga").addQuoted("cirrus");
		command.addLiteral("-usb");
		command.addLiteral("-readconfig").addFile(getDeviceConfigPath());

		if (!disableKvm) {
			command.addLiteral("-enable-kvm");
		}

		SupervisorProcessConfig sup = new SupervisorProcessConfig(key);
		Map<String, String> properties = sup.getProperties();
		properties.put("command", command.buildCommandString());

		ManagedSupervisorInstance instance = injected(ManagedSupervisorInstance.class);
		instance.config = sup;
		return instance;
	}
}
