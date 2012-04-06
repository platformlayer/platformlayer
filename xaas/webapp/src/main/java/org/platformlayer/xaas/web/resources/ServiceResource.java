package org.platformlayer.xaas.web.resources;

import java.io.IOException;
import java.io.StringWriter;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.platformlayer.CheckedCallable;
import org.platformlayer.RepositoryException;
import org.platformlayer.crypto.OpenSshUtils;
import org.platformlayer.ids.ItemType;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.tasks.OpsContextBuilder;
import org.platformlayer.xaas.services.ModelClass;
import org.platformlayer.xaas.services.ServiceProvider;
import org.platformlayer.xml.MemorySchemaOutputResolver;

import com.google.common.collect.Lists;

public class ServiceResource extends XaasResourceBase {
    @Inject
    Provider<OpsContextBuilder> opsContextBuilderFactory;

    @GET
    @Path("schema")
    @Produces({ XML })
    public String getSchema() throws IOException, JAXBException {
        ServiceProvider serviceProvider = getServiceProvider();

        String namespace = null;

        List<Class<?>> javaClasses = Lists.newArrayList();
        for (ModelClass<?> modelClass : serviceProvider.getModels().all()) {
            javaClasses.add(modelClass.getJavaClass());
            String modelNamespace = modelClass.getPrimaryNamespace();
            if (namespace == null) {
                namespace = modelNamespace;
            } else if (!namespace.equals(modelNamespace)) {
                throw new IllegalStateException();
            }
        }

        JAXBContext jaxbContext = JAXBContext.newInstance(javaClasses.toArray(new Class<?>[javaClasses.size()]));

        MemorySchemaOutputResolver schemaOutputResolver = new MemorySchemaOutputResolver();
        jaxbContext.generateSchema(schemaOutputResolver);

        Map<String, StringWriter> writers = schemaOutputResolver.getWriters();
        StringWriter writer = writers.get(namespace);
        if (writer == null)
            throw new IllegalArgumentException();

        return writer.getBuffer().toString();
    }

    @GET
    @Path("sshkey")
    @Produces({ TEXT_PLAIN })
    public String getSshPublicKey() throws RepositoryException, OpsException, IOException {
        final ServiceProvider serviceProvider = getServiceProvider();

        OpsContextBuilder opsContextBuilder = opsContextBuilderFactory.get();
        final OpsContext opsContext = opsContextBuilder.buildOpsContext(serviceProvider.getServiceType(), getAuthentication(), null);

        PublicKey publicKey = OpsContext.runInContext(opsContext, new CheckedCallable<PublicKey, Exception>() {
            @Override
            public PublicKey call() throws Exception {
                PublicKey publicKey = serviceProvider.getSshPublicKey();
                return publicKey;
            }
        });

        if (publicKey == null) {
            throw new WebApplicationException(404);
        }

        return OpenSshUtils.serialize(publicKey);
    }

    @Path("{itemType}")
    public XaasResourceBase getManagedCollectionResource(@PathParam("itemType") String itemType) {
        getScope().put(new ItemType(itemType));

        XaasResourceBase resources = objectInjector.getInstance(ManagedItemCollectionResource.class);
        return resources;
    }

}
