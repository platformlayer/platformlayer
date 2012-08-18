package org.platformlayer.xaas.web.resources;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;

import org.platformlayer.CheckedCallable;
import org.platformlayer.RepositoryException;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.metrics.model.MetricDataSource;
import org.platformlayer.metrics.model.MetricInfoCollection;
import org.platformlayer.metrics.model.MetricQuery;
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

		final OpsContext opsContext = opsContextBuilder.buildTemporaryOpsContext(getServiceType(),
				getProjectAuthorization());

		return OpsContext.runInContext(opsContext, new CheckedCallable<MetricInfoCollection, Exception>() {
			@Override
			public MetricInfoCollection call() throws Exception {
				BindingScope bindingScope = BindingScope.push(managedItem, managedItem);
				try {
					Object controller = serviceProvider.getController(managedItem);
					MetricConfig metricConfig = opsContext.getMetricInfo(controller);

					return MetricCollector.toMetricInfo(metricConfig);
				} finally {
					bindingScope.pop();
				}
			}
		});
	}

	@POST
	@Produces({ XML, JSON })
	public MetricDataSource getMetrics(final MetricQuery query) throws RepositoryException, OpsException {
		final ItemBase managedItem = getManagedItem();
		final ServiceProvider serviceProvider = getServiceProvider();

		OpsContextBuilder opsContextBuilder = objectInjector.getInstance(OpsContextBuilder.class);

		final OpsContext opsContext = opsContextBuilder.buildTemporaryOpsContext(getServiceType(),
				getProjectAuthorization());

		return OpsContext.runInContext(opsContext, new CheckedCallable<MetricDataSource, Exception>() {
			@Override
			public MetricDataSource call() throws Exception {
				BindingScope bindingScope = BindingScope.push(managedItem, managedItem);
				try {
					MetricDataSource metrics = serviceProvider.getMetricValues(managedItem, query);
					return metrics;
				} finally {
					bindingScope.pop();
				}
			}
		});
	}
}
