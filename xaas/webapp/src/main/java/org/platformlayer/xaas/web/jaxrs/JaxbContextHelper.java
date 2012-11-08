package org.platformlayer.xaas.web.jaxrs;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.platformlayer.core.model.BackupAction;
import org.platformlayer.core.model.ConfigureAction;
import org.platformlayer.core.model.DeleteAction;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.JobSchedule;
import org.platformlayer.core.model.ManagedItemCollection;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.ServiceInfo;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;
import org.platformlayer.core.model.ValidateAction;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.xaas.services.ModelClass;
import org.platformlayer.xaas.services.ServiceProvider;
import org.platformlayer.xaas.services.ServiceProviderDictionary;

import com.google.common.collect.Lists;

@Singleton
public class JaxbContextHelper implements Provider<JAXBContext> {

	@Inject
	ServiceProviderDictionary serviceProviderDictionary;

	// TODO: Separate JAXBContexts per service, to avoid namespace pollution
	JAXBContext jaxbContext;

	public synchronized JAXBContext getJaxbContext(Class<?> forClass) {
		if (jaxbContext == null) {
			List<Class<?>> javaClasses = Lists.newArrayList();

			javaClasses.add(Tags.class);
			javaClasses.add(Tag.class);
			javaClasses.add(ItemBase.class);
			javaClasses.add(ManagedItemCollection.class);
			javaClasses.add(JobSchedule.class);
			javaClasses.add(PlatformLayerKey.class);

			javaClasses.add(ConfigureAction.class);
			javaClasses.add(ValidateAction.class);
			javaClasses.add(DeleteAction.class);
			javaClasses.add(BackupAction.class);

			for (ServiceInfo serviceInfo : serviceProviderDictionary.getAllServices()) {
				ServiceType serviceType = new ServiceType(serviceInfo.serviceType);
				ServiceProvider serviceProvider = serviceProviderDictionary.getServiceProvider(serviceType);

				for (ModelClass<?> modelClass : serviceProvider.getModels().all()) {
					javaClasses.add(modelClass.getJavaClass());
					// String modelNamespace = modelClass.getJaxbHelper().getPrimaryNamespace();
					// if (namespace == null) {
					// namespace = modelNamespace;
					// } else if (!namespace.equals(modelNamespace)) {
					// throw new IllegalStateException();
					// }
				}

				for (Class<?> clazz : serviceProvider.getActions()) {
					javaClasses.add(clazz);
				}
			}

			try {
				jaxbContext = JAXBContext.newInstance(javaClasses.toArray(new Class<?>[javaClasses.size()]));
			} catch (JAXBException e) {
				throw new IllegalStateException("Unable to build JAXB context", e);
			}
		}

		return jaxbContext;
	}

	@Override
	public JAXBContext get() {
		return getJaxbContext(null);
	}

}
