package org.platformlayer.xaas.web.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.platformlayer.CheckedCallable;
import org.platformlayer.RepositoryException;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.metrics.model.MetricInfoCollection;
import org.platformlayer.metrics.model.MetricValues;
import org.platformlayer.ops.BindingScope;
import org.platformlayer.ops.MetricCollector;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.model.metrics.MetricConfig;
import org.platformlayer.ops.tasks.OpsContextBuilder;
import org.platformlayer.xaas.services.ServiceProvider;

public class MetricsResource extends XaasResourceBase {
    @GET
    @Produces({ XML, JSON })
    public MetricInfoCollection listMetrics() throws OpsException, RepositoryException {
        final ItemBase managedItem = getManagedItem();
        final ServiceProvider serviceProvider = getServiceProvider();

        OpsContextBuilder opsContextBuilder = objectInjector.getInstance(OpsContextBuilder.class);

        final OpsContext opsContext = opsContextBuilder.buildOpsContext(getServiceType(), getAuthentication(), null);

        return OpsContext.runInContext(opsContext, new CheckedCallable<MetricInfoCollection, Exception>() {
            @Override
            public MetricInfoCollection call() throws Exception {
                BindingScope bindingScope = BindingScope.push(managedItem, managedItem);
                try {
                    Object controller = serviceProvider.getController(managedItem.getClass());
                    MetricConfig metricConfig = opsContext.getMetricInfo(controller);

                    return MetricCollector.toMetricInfo(metricConfig);
                } finally {
                    bindingScope.pop();
                }
            }
        });
    }

    @GET
    @Path("{metricKey}")
    @Produces({ XML, JSON })
    public MetricValues getMetrics(@PathParam("metricKey") final String metricKey) throws RepositoryException, OpsException {
        final ItemBase managedItem = getManagedItem();
        final ServiceProvider serviceProvider = getServiceProvider();

        OpsContextBuilder opsContextBuilder = objectInjector.getInstance(OpsContextBuilder.class);

        final OpsContext opsContext = opsContextBuilder.buildOpsContext(getServiceType(), getAuthentication(), null);

        return OpsContext.runInContext(opsContext, new CheckedCallable<MetricValues, Exception>() {
            @Override
            public MetricValues call() throws Exception {
                BindingScope bindingScope = BindingScope.push(managedItem, managedItem);
                try {

                    MetricValues metrics = serviceProvider.getMetricValues(managedItem, metricKey);
                    return metrics;
                } finally {
                    bindingScope.pop();
                }
            }
        });
    }
}
