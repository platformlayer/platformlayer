package org.platformlayer.service.cloud.direct.ops;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import org.apache.log4j.Logger;
import org.openstack.utils.Utf8;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.SyntheticFile;
import org.platformlayer.ops.machines.InetAddressUtils;
import org.platformlayer.ops.networks.AddressModel;
import org.platformlayer.ops.networks.ScriptBuilder;
import org.platformlayer.ops.supervisor.ManagedSupervisorInstance;
import org.platformlayer.ops.supervisor.SupervisorProcessConfig;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.util.Providers;

public class InstanceScript extends SyntheticFile {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(InstanceScript.class);

	public String key;
	public Command launchInstanceCommand;
	public List<Provider<AddressModel>> addresses = Lists.newArrayList();

	public String primaryInterface;

	public InstanceScript() {
		this.fileMode = "755";
	}

	@Override
	protected byte[] getContentsBytes() throws OpsException {
		ScriptBuilder sb = new ScriptBuilder();

		addIpNeighborProxy(sb);

		sb.add(launchInstanceCommand);

		String script = sb.toString();

		return Utf8.getBytes(script);
	}

	private void addIpNeighborProxy(ScriptBuilder sb) throws OpsException {
		if (addresses != null) {
			for (Provider<AddressModel> addressProvider : addresses) {
				AddressModel addressModel = addressProvider.get();
				InetAddress address = addressModel.getInetAddress();
				if (InetAddressUtils.isIpv6(address)) {
					if (Strings.isNullOrEmpty(primaryInterface)) {
						throw new OpsException("primaryInterface not specified");
					}

					Command command = Command.build("ip neigh add proxy {0} dev {1}", address, primaryInterface);
					sb.add(command);
				}
			}
		}
	}

	public void configure(ManagedSupervisorInstance instance) {
		SupervisorProcessConfig sup = new SupervisorProcessConfig(key);
		Map<String, String> properties = sup.getProperties();

		Command command = Command.build(filePath.getAbsolutePath());
		properties.put("command", command.buildCommandString());

		instance.config = Providers.of(sup);
	}

}
