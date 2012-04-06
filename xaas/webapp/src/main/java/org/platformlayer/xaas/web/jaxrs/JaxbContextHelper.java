package org.platformlayer.xaas.web.jaxrs;

import java.util.List;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ManagedItemCollection;
import org.platformlayer.core.model.ServiceInfo;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.xaas.services.ModelClass;
import org.platformlayer.xaas.services.ServiceProvider;
import org.platformlayer.xaas.services.ServiceProviderDictionary;

import com.google.common.collect.Lists;

public class JaxbContextHelper {

    @Inject
    ServiceProviderDictionary serviceProviderDictionary;

    // TODO: Separate JAXBContexts per service, to avoid namespace pollution
    JAXBContext jaxbContext;

    public synchronized JAXBContext getJaxbContext(Class<?> clazz) {
        if (jaxbContext == null) {
            List<Class<?>> javaClasses = Lists.newArrayList();

            javaClasses.add(Tags.class);
            javaClasses.add(Tag.class);
            javaClasses.add(ItemBase.class);
            javaClasses.add(ManagedItemCollection.class);

            boolean management = false;
            for (ServiceInfo serviceInfo : serviceProviderDictionary.getAllServices(management)) {
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
            }

            try {
                jaxbContext = JAXBContext.newInstance(javaClasses.toArray(new Class<?>[javaClasses.size()]));
            } catch (JAXBException e) {
                throw new IllegalStateException("Unable to build JAXB context", e);
            }
        }

        return jaxbContext;
    }

}
