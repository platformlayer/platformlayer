package org.platformlayer.service.instancesupervisor;

import java.util.List;

import org.platformlayer.instances.model.PersistentInstance;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.ServiceProviderBase;
import org.platformlayer.service.instancesupervisor.ops.PersistentInstanceController;
import org.platformlayer.xaas.Service;
import org.platformlayer.xaas.services.ModelClass;

@Service("instancesupervisor")
public class InstanceSupervisorProvider extends ServiceProviderBase {
	@Override
	protected List<ModelClass<?>> buildModels() {
		List<ModelClass<?>> modelClasses = super.buildModels();

		modelClasses.add(ModelClass.publicModel(this, PersistentInstance.class));

		return modelClasses;
	}

	@Override
	public Class<?> getControllerClass(Class<?> managedItemClass) throws OpsException {
		ensureInitialized();

		if (managedItemClass == PersistentInstance.class) {
			return PersistentInstanceController.class;
		}

		return super.getControllerClass(managedItemClass);
	}
}
