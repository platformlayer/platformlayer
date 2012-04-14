package org.platformlayer.ops;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.platformlayer.metrics.model.MetricInfo;
import org.platformlayer.metrics.model.MetricInfoCollection;
import org.platformlayer.ops.model.metrics.Metric;
import org.platformlayer.ops.model.metrics.MetricConfig;

import com.google.common.collect.Lists;

public class MetricCollector {

	final List<MetricConfig> metricConfigs = Lists.newArrayList();

	public MetricConfig getMetricInfo(Object target) throws OpsException {
		visit(target);

		MetricConfig metricConfig = new MetricConfig();
		metricConfig.metrics = metricConfigs;
		return metricConfig;
	}

	void visit(Object target) throws OpsException {
		visitMethods(target);
		visitChildren(target);
	}

	private void visitChildren(Object target) throws OpsException {
		if (target instanceof OpsTree) {
			OpsTree tree = (OpsTree) target;
			for (Object child : tree.getChildren()) {
				visit(child);
			}
		}
	}

	void visitMethods(Object target) throws OpsException {
		for (Method method : target.getClass().getMethods()) {
			Class<?> returnType = method.getReturnType();
			if (returnType.equals(MetricConfig.class)) {
				MetricConfig metricConfig;
				try {
					metricConfig = (MetricConfig) method.invoke(target, (Object[]) null);
				} catch (IllegalArgumentException e) {
					throw new OpsException("Error invoking method: " + method, e);
				} catch (IllegalAccessException e) {
					throw new OpsException("Error invoking method: " + method, e);
				} catch (InvocationTargetException e) {
					throw new OpsException("Error invoking method: " + method, e);
				}
				metricConfigs.add(metricConfig);
			}
		}
	}

	static void populateMetricInfo(MetricInfoCollection metricInfoCollection, String prefix, MetricConfig metrics) {
		if (metrics.metric != null) {
			for (Metric metric : metrics.metric) {
				MetricInfo metricInfo = new MetricInfo();
				metricInfo.key = (prefix + metric.key);
				metricInfoCollection.metricInfoList.add(metricInfo);
			}
		}

		if (metrics.metrics != null) {
			for (MetricConfig subtree : metrics.metrics) {
				String childPrefix = prefix + subtree.key + "/";

				populateMetricInfo(metricInfoCollection, childPrefix, subtree);
			}
		}
	}

	public static MetricInfoCollection toMetricInfo(MetricConfig metricConfig) {
		MetricInfoCollection metricInfo = new MetricInfoCollection();

		populateMetricInfo(metricInfo, "", metricConfig);

		return metricInfo;
	}

}
