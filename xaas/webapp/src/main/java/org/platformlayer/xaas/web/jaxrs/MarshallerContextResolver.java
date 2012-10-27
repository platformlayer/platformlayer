package org.platformlayer.xaas.web.jaxrs;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ManagedItemCollection;

@Provider
@Consumes({ javax.ws.rs.core.MediaType.APPLICATION_XML, javax.ws.rs.core.MediaType.APPLICATION_JSON })
@Produces({ javax.ws.rs.core.MediaType.APPLICATION_XML, javax.ws.rs.core.MediaType.APPLICATION_JSON })
public class MarshallerContextResolver implements ContextResolver<Marshaller> {

	@Inject
	JaxbContextHelper jaxbContextHelper;

	@Override
	public Marshaller getContext(Class<?> clazz) {
		if (clazz.equals(ManagedItemCollection.class)) {
			// OK
		} else if (ItemBase.class.isAssignableFrom(clazz)) {
			// OK
		} else {
			return null;
		}

		JAXBContext jaxbContext = jaxbContextHelper.getJaxbContext(clazz);

		// TODO: Is this expensive? Pool marshallers?

		try {
			Marshaller m = jaxbContext.createMarshaller();
			m.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl());
			return m;
		} catch (JAXBException e) {
			throw new IllegalStateException("Error creating XML marshaller", e);
		}
	}
}