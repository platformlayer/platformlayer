package org.platformlayer.metrics.client;

import java.io.IOException;
import java.lang.Thread.State;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Metered;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricProcessor;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Sampling;
import com.yammer.metrics.core.Summarizable;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.VirtualMachineMetrics;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import com.yammer.metrics.stats.Snapshot;

public class PlatformlayerMetricsReporter extends AbstractPollingReporter implements MetricProcessor<MetricTree> {
	private static final Logger LOG = LoggerFactory.getLogger(PlatformlayerMetricsReporter.class);

	protected final MetricPredicate predicate;
	protected final Clock clock;
	protected final VirtualMachineMetrics vm;
	public boolean printVMMetrics = true;

	final MetricClient metricSender;

	Long previousRun = null;

	public static void enable(long period, TimeUnit unit, MetricClient metricSender) {
		enable(Metrics.defaultRegistry(), period, unit, metricSender);
	}

	public static void enable(MetricsRegistry metricsRegistry, long period, TimeUnit unit, MetricClient metricSender) {
		enable(metricsRegistry, period, unit, metricSender, null);
	}

	public static void enable(long period, TimeUnit unit, MetricClient metricSender, String prefix) {
		enable(Metrics.defaultRegistry(), period, unit, metricSender, prefix);
	}

	public static void enable(MetricsRegistry metricsRegistry, long period, TimeUnit unit, MetricClient metricSender,
			String prefix) {
		enable(metricsRegistry, period, unit, metricSender, prefix, MetricPredicate.ALL);
	}

	public static void enable(MetricsRegistry metricsRegistry, long period, TimeUnit unit, MetricClient metricSender,
			String prefix, MetricPredicate predicate) {
		try {
			final PlatformlayerMetricsReporter reporter = new PlatformlayerMetricsReporter(metricsRegistry, prefix,
					predicate, metricSender, Clock.defaultClock());
			reporter.start(period, unit);
		} catch (Exception e) {
			LOG.error("Error creating/starting PlatformLayer reporter:", e);
		}
	}

	public PlatformlayerMetricsReporter(MetricClient metricSender, String prefix) throws IOException {
		this(Metrics.defaultRegistry(), metricSender, prefix);
	}

	public PlatformlayerMetricsReporter(MetricsRegistry metricsRegistry, MetricClient metricSender, String prefix)
			throws IOException {
		this(metricsRegistry, prefix, MetricPredicate.ALL, metricSender, Clock.defaultClock());
	}

	public PlatformlayerMetricsReporter(MetricsRegistry metricsRegistry, String prefix, MetricPredicate predicate,
			MetricClient metricSender, Clock clock) throws IOException {
		this(metricsRegistry, prefix, predicate, metricSender, clock, VirtualMachineMetrics.getInstance());
	}

	public PlatformlayerMetricsReporter(MetricsRegistry metricsRegistry, String prefix, MetricPredicate predicate,
			MetricClient metricSender, Clock clock, VirtualMachineMetrics vm) throws IOException {
		this(metricsRegistry, prefix, predicate, metricSender, clock, vm, "platformlayer-reporter");
	}

	public PlatformlayerMetricsReporter(MetricsRegistry metricsRegistry, String prefix, MetricPredicate predicate,
			MetricClient metricSender, Clock clock, VirtualMachineMetrics vm, String name) throws IOException {
		super(metricsRegistry, name);
		this.metricSender = metricSender;
		this.vm = vm;

		this.clock = clock;

		this.predicate = predicate;
	}

	@Override
	public void run() {
		long now = clock.time() / 1000;
		if (previousRun == null) {
			// TODO: Zero metrics??
			previousRun = now;
			return;
		}

		MetricTree tree = new MetricTree();

		addTimestampRange(tree, previousRun, now);

		// final long epoch = clock.time() / 1000;
		if (this.printVMMetrics) {
			printVmMetrics(tree);
		}
		printRegularMetrics(tree);

		if (metricSender.sendMetrics(tree)) {
			// TODO: Zero metrics??
			previousRun = now;
		}
	}

	// private void consumeResponse(HttpPost request, HttpResponse response) throws IOException {
	// HttpEntity entity = response.getEntity();
	//
	// // If the response does not enclose an entity, there is no need
	// // to worry about connection release
	// if (entity != null) {
	// InputStream instream = null;
	// try {
	// instream = entity.getContent();
	//
	// // BufferedReader reader = new BufferedReader(
	// // new InputStreamReader(instream));
	// // // do something useful with the response
	// // System.out.println(reader.readLine());
	// } catch (IOException ex) {
	//
	// // In case of an IOException the connection will be released
	// // back to the connection manager automatically
	// throw ex;
	//
	// } catch (RuntimeException ex) {
	// // In case of an unexpected exception you may want to abort
	// // the HTTP request in order to shut down the underlying
	// // connection and release it back to the connection manager.
	// request.abort();
	// throw ex;
	// } finally {
	// // Closing the input stream will trigger connection release
	// IoUtils.safeClose(instream);
	// }
	// }
	//
	// }

	private void addTimestampRange(MetricTree tree, long previous, long now) {
		tree.addInt("t0", previous);
		tree.addInt("t1", now);
	}

	protected void printRegularMetrics(final MetricTree tree) {
		for (Entry<String, SortedMap<MetricName, Metric>> entry : getMetricsRegistry().groupedMetrics(predicate)
				.entrySet()) {
			for (Entry<MetricName, Metric> subEntry : entry.getValue().entrySet()) {
				final Metric metric = subEntry.getValue();
				if (metric != null) {
					try {
						MetricDispatcher.dispatch(subEntry.getValue(), subEntry.getKey(), this, tree);
					} catch (Exception ignored) {
						LOG.error("Error printing regular metrics:", ignored);
					}
				}
			}
		}
	}

	@Override
	public void processGauge(MetricName name, Gauge<?> gauge, MetricTree tree) throws IOException {
		MetricTree subtree = tree.getSubtree(name);

		Object value = gauge.value();
		if (value instanceof Number) {
			Number number = (Number) value;
			if (value instanceof Float || value instanceof Double) {
				double v = number.doubleValue();
				subtree.addFloat("value", v);
			} else {
				long v = number.longValue();
				subtree.addFloat("value", v);
			}
		} else {
			LOG.info("Skipping metric: " + name);
		}
	}

	@Override
	public void processCounter(MetricName name, Counter counter, MetricTree tree) throws IOException {
		MetricTree subtree = tree.getSubtree(name);

		subtree.addInt("count", counter.count());
	}

	@Override
	public void processMeter(MetricName name, Metered meter, MetricTree tree) throws IOException {
		MetricTree subtree = tree.getSubtree(name);

		// final String sanitizedName = sanitizeName(name);
		subtree.addInt("count", meter.count());
		subtree.addFloat("meanRate", meter.meanRate());
		subtree.addFloat("1MinuteRate", meter.oneMinuteRate());
		subtree.addFloat("5MinuteRate", meter.fiveMinuteRate());
		subtree.addFloat("15MinuteRate", meter.fifteenMinuteRate());
	}

	@Override
	public void processHistogram(MetricName name, Histogram histogram, MetricTree tree) throws IOException {
		MetricTree subtree = tree.getSubtree(name);

		// final String sanitizedName = sanitizeName(name);
		sendSummarizable(subtree, histogram);
		sendSampling(subtree, histogram);
	}

	@Override
	public void processTimer(MetricName name, Timer timer, MetricTree tree) throws IOException {
		processMeter(name, timer, tree);
		// final String sanitizedName = sanitizeName(name);
		MetricTree subtree = tree.getSubtree(name);

		sendSummarizable(subtree, timer);
		sendSampling(subtree, timer);
	}

	protected void sendSummarizable(MetricTree subtree, Summarizable metric) throws IOException {
		subtree.addFloat("min", metric.min());
		subtree.addFloat("max", metric.max());
		subtree.addFloat("mean", metric.mean());
		subtree.addFloat("stddev", metric.stdDev());
	}

	protected void sendSampling(MetricTree subtree, Sampling metric) throws IOException {
		final Snapshot snapshot = metric.getSnapshot();
		subtree.addFloat("median", snapshot.getMedian());
		subtree.addFloat("75percentile", snapshot.get75thPercentile());
		subtree.addFloat("95percentile", snapshot.get95thPercentile());
		subtree.addFloat("98percentile", snapshot.get98thPercentile());
		subtree.addFloat("99percentile", snapshot.get99thPercentile());
		subtree.addFloat("999percentile", snapshot.get999thPercentile());
	}

	protected void printVmMetrics(MetricTree tree) {
		MetricTree jvmTree = tree.getSubtree("jvm");

		MetricTree memoryTree = jvmTree.getSubtree("memory");

		memoryTree.addFloat("heap_usage", vm.heapUsage());
		memoryTree.addFloat("non_heap_usage", vm.nonHeapUsage());

		MetricTree memoryPoolUsages = memoryTree.getSubtree("memory_pool_usages");
		for (Entry<String, Double> pool : vm.memoryPoolUsage().entrySet()) {
			memoryPoolUsages.addFloat(pool.getKey(), pool.getValue());
		}

		jvmTree.addInt("daemon_thread_count", vm.daemonThreadCount());
		jvmTree.addInt("thread_count", vm.threadCount());
		jvmTree.addInt("uptime", vm.uptime());
		jvmTree.addFloat("fd_usage", vm.fileDescriptorUsage());

		MetricTree threadStates = jvmTree.getSubtree("thread-states");
		for (Entry<State, Double> entry : vm.threadStatePercentages().entrySet()) {
			threadStates.addFloat(entry.getKey().toString().toLowerCase(), entry.getValue());
		}

		MetricTree gcTree = jvmTree.getSubtree("gc");
		for (Entry<String, VirtualMachineMetrics.GarbageCollectorStats> entry : vm.garbageCollectors().entrySet()) {
			MetricTree collectorTree = gcTree.getSubtree(entry.getKey());
			collectorTree.addInt("time", entry.getValue().getTime(TimeUnit.MILLISECONDS));
			collectorTree.addInt("runs", entry.getValue().getRuns());
		}
	}
}