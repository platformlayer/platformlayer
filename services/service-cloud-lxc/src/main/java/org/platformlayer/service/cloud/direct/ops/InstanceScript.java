package org.platformlayer.service.cloud.direct.ops;

import java.net.InetAddress;
import java.util.List;

import javax.inject.Provider;

import org.platformlayer.core.model.AddressModel;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsProvider;
import org.platformlayer.ops.filesystem.SyntheticFile;
import org.platformlayer.ops.machines.InetAddressUtils;
import org.platformlayer.ops.networks.ScriptBuilder;
import org.platformlayer.ops.supervisor.StandardService;
import org.platformlayer.service.cloud.direct.model.DirectHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.Utf8;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class InstanceScript extends SyntheticFile {

	private static final Logger log = LoggerFactory.getLogger(InstanceScript.class);

	public String key;
	public Command launchInstanceCommand;
	public List<Provider<AddressModel>> addresses = Lists.newArrayList();

	public String hostPrimaryInterface;

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
					String hostPrimaryInterface = getHostPrimaryInterface();
					if (Strings.isNullOrEmpty(hostPrimaryInterface)) {
						throw new OpsException("primaryInterface not specified");
					}

					Command command = Command.build("ip neigh add proxy {0} dev {1}", address, hostPrimaryInterface);
					sb.add(command);
				}
			}
		}
	}

	private String getHostPrimaryInterface() {
		if (hostPrimaryInterface != null) {
			return hostPrimaryInterface;
		}

		DirectHost host = OpsContext.get().getInstance(DirectHost.class);
		return host.publicInterface;
	}

	// public void configure(ManagedSupervisordInstance instance) {
	// SupervisorProcessConfig sup = new SupervisorProcessConfig(key);
	// Map<String, String> properties = sup.getProperties();
	//
	// Command command = Command.build(filePath.getAbsolutePath());
	// properties.put("command", command.buildCommandString());
	//
	// instance.config = Providers.of(sup);
	// }

	public void configure(ItemBase owner, StandardService service) {
		Command command = Command.build(filePath.getAbsolutePath());
		service.command = OpsProvider.of(command);

		service.key = key;
		service.owner = owner.getKey();

		// Map<String, String> env = template.getEnvironment();
		// service.environment = Providers.of(env);
		//
		// service.instanceDir = instanceDir;
		// service.key = template.getServiceKey();
		//
		// service.owner = template.getModel().getKey();
		//
		// service.matchExecutableName = template.getMatchExecutableName();
	}

}
