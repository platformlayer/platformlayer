package org.platformlayer.keystone.cli.formatters;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.platformlayer.auth.OpsProject;

import com.fathomdb.cli.formatter.SimpleFormatter;
import com.fathomdb.cli.output.OutputSink;
import com.google.common.collect.Maps;

public class OpsProjectFormatter extends SimpleFormatter<OpsProject> {

	public OpsProjectFormatter() {
		super(OpsProject.class);
	}

	@Override
	public void visit(OpsProject o, OutputSink sink) throws IOException {
		LinkedHashMap<String, Object> values = Maps.newLinkedHashMap();

		values.put("key", o.key);

		sink.outputRow(values);
	}
}
