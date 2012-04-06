package org.platformlayer.xaas.web.jaxrs;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ManagedItemCollection;

@Provider
@Consumes({ javax.ws.rs.core.MediaType.APPLICATION_XML, javax.ws.rs.core.MediaType.APPLICATION_JSON })
@Produces({ javax.ws.rs.core.MediaType.APPLICATION_XML, javax.ws.rs.core.MediaType.APPLICATION_JSON })
public class JaxbContextResolver implements ContextResolver<JAXBContext> {

    // private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    // private ObjectMapper mapper = new ObjectMapper();
    //
    // public JaxbContextResolver() {
    // SerializationConfig serConfig = mapper.getSerializationConfig();
    // SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    // dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    //
    // serConfig.setDateFormat(dateFormat);
    //
    // DeserializationConfig deserializationConfig = mapper.getDeserializationConfig();
    // deserializationConfig.setDateFormat(dateFormat);
    //
    // mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
    // }

    @Inject
    JaxbContextHelper jaxbContextHelper;

    @Override
    public JAXBContext getContext(Class<?> clazz) {
        // if (clazz != JAXBContext.class) {
        // return null;
        // }

        if (clazz.equals(ManagedItemCollection.class)) {
            // OK
        } else if (ItemBase.class.isAssignableFrom(clazz)) {
            // OK
        } else {
            return null;
        }

        return jaxbContextHelper.getJaxbContext(clazz);
    }
}