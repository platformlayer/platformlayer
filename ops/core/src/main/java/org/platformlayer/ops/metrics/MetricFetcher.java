package org.platformlayer.ops.metrics;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.metrics.model.MetricDataSource;
import org.platformlayer.metrics.model.MetricQuery;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.ServiceProviderBase;

public interface MetricFetcher {
	MetricDataSource fetch(ServiceProviderBase serviceProviderBase, ItemBase managedItem, MetricQuery query)
			throws OpsException;
}
