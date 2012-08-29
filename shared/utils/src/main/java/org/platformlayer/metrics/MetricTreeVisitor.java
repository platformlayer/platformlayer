package org.platformlayer.metrics;

import org.platformlayer.metrics.MetricTreeBase.MetricTreeArray;
import org.platformlayer.metrics.MetricTreeBase.MetricTreeFloat;
import org.platformlayer.metrics.MetricTreeBase.MetricTreeInteger;
import org.platformlayer.metrics.MetricTreeBase.MetricTreeString;

public class MetricTreeVisitor {
	public void visit(MetricTreeObject o) {
		o.visitChildren(this);
	}

	public void visit(MetricTreeString o) {
	}

	public void visit(MetricTreeArray o) {
		o.visitItems(this);
	}

	public void visit(MetricTreeInteger o) {
	}

	public void visit(MetricTreeFloat o) {
	}
}
