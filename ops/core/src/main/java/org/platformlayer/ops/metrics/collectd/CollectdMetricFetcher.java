package org.platformlayer.ops.metrics.collectd;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import javax.inject.Inject;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.metrics.model.MetricDataSource;
import org.platformlayer.metrics.model.MetricValues;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.ServiceProviderBase;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.helpers.SshKey;
import org.platformlayer.ops.helpers.SshKeys;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.metrics.MetricFetcher;
import org.platformlayer.ops.model.metrics.Metric;
import org.platformlayer.ops.model.metrics.MetricConfig;
import org.platformlayer.ops.process.ProcessExecution;
import org.platformlayer.service.collectd.v1.CollectdService;
import org.platformlayer.xml.JaxbHelper;
import org.platformlayer.xml.UnmarshalException;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

public class CollectdMetricFetcher implements MetricFetcher {
	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	InstanceHelpers instances;

	@Inject
	SshKeys sshKeys;

	@Inject
	OpsContext ops;

	@Override
	public MetricDataSource fetch(ServiceProviderBase serviceProviderBase, ItemBase managedItem, String metricKey)
			throws OpsException {
		Object controller = serviceProviderBase.getController(managedItem);

		MetricConfig metricConfig = ops.getMetricInfo(controller);

		Metric metric = getMetricConfig(metricConfig, metricKey);
		if (metric == null) {
			return null;
		}

		String collectdMachineKey = null;
		{
			// TODO: Multiple machines per service
			PlatformLayerKey modelKey = managedItem.getKey();
			collectdMachineKey = CollectdHelpers.toCollectdKey(modelKey);
		}

		// OpenstackComputeMachine itemMachine = instances.findMachine(managedItem);
		// if (itemMachine != null) {
		// collectdMachineKey = itemMachine.getAddress();
		// }

		if (collectdMachineKey == null) {
			return null;
		}

		for (CollectdService collectdService : platformLayer.listItems(CollectdService.class)) {
			Machine machine = instances.findMachine(collectdService);

			// TODO: This is so evil...
			SshKey collectdKey = sshKeys.findOtherServiceKey(new ServiceType("collectd"));

			if (collectdKey == null) {
				throw new OpsException("Cannot find SSH key to query collectd");
			}

			OpsTarget target = machine.getTarget(collectdKey);

			String metricDefName = metricKey.replace(".", "__");

			String filePath = "/var/lib/rrdcached/db/collectd/" + collectdMachineKey + "/" + metric.rrd;
			String defineCommand = "DEF:" + metricDefName + "=" + filePath + ":value:AVERAGE";
			String exportCommand = "XPORT:" + metricDefName + ":" + metricDefName;
			Command command = Command.build("rrdtool xport  --daemon unix:/var/run/rrdcached.sock {0} {1} | gzip -f",
					defineCommand, exportCommand);
			ProcessExecution execution = target.executeCommand(command);

			MetricValues metricValues;
			try {
				byte[] stdoutGzipped = execution.getBinaryStdOut();
				GZIPInputStream stdout = new GZIPInputStream(new ByteArrayInputStream(stdoutGzipped));

				String stderr = execution.getStdErr();

				if (!Strings.isNullOrEmpty(stderr)) {
					throw new OpsException("Error fetching rrd data: " + stderr);
				}
				// MetricValues uses the same XML format as RRD (for now)
				metricValues = JaxbHelper.deserializeXmlObject(stdout, MetricValues.class, false);
				// String xml = IoUtils.readAll(stdout);
				// metricValues = JaxbHelper.deserializeXmlObject(xml, MetricValues.class);
			} catch (IOException e) {
				throw new OpsException("Error expanding output", e);
			} catch (UnmarshalException e) {
				throw new OpsException("Error parsing RRD output", e);
			}

			// TODO: We're deserializing the RRD, only to reserialize it to the exact same format

			return toMetricDataSource(metricValues);
		}

		// No data found
		return null;
	}

	private MetricDataSource toMetricDataSource(MetricValues metricValues) {
		throw new UnsupportedOperationException();
	}

	private Metric getMetricConfig(MetricConfig metricConfig, String metricKey) throws OpsException {
		String[] components = metricKey.split("\\.");

		if (components.length == 0) {
			throw new IllegalArgumentException();
		}

		int position = 0;
		while (true) {
			String component = components[position];
			if ((position + 1) == components.length) {
				if (metricConfig.metric != null) {
					for (Metric metric : metricConfig.metric) {
						if (Objects.equal(metric.key, component)) {
							return metric;
						}
					}
				}
			} else {
				boolean found = false;

				if (metricConfig.metrics != null) {
					for (MetricConfig tree : metricConfig.metrics) {
						if (Objects.equal(tree.key, component)) {
							position++;
							metricConfig = tree;
							found = true;
							break;
						}
					}
				}

				if (found) {
					continue;
				}
			}

			return null;
		}
	}

}
