package org.platformlayer.ops.supervisor;

import java.io.File;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.tree.OpsTreeBase;

import com.google.inject.util.Providers;

public class StandardService extends OpsTreeBase {
	public PlatformLayerKey owner;

	public String key;
	public Provider<Command> command;
	public Provider<Map<String, String>> environment;
	public File instanceDir;
	public String user;
	public String matchExecutableName;

	@Inject
	ServiceManager serviceManager;

	@Handler
	public void handler() {
	}

	public String getServiceId() {
		return key;
	}

	public File getLogFile() {
		File logFile = new File("/var/log/" + key + ".log");
		return logFile;
	}

	public PlatformLayerKey getOwner() throws OpsException {
		if (owner != null) {
			return owner;
		}
		ItemBase itemBase = OpsContext.get().getInstance(ItemBase.class);
		if (itemBase == null) {
			throw new OpsException("No owner in scope");
		}
		return itemBase.getKey();
	}

	@Override
	protected void addChildren() throws OpsException {
		serviceManager.addServiceInstance(this);
	}

	public void setCommand(Command command) {
		this.command = Providers.of(command);
	}
}
