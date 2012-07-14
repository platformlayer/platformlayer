package org.platformlayer.keystone.cli.formatters;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.platformlayer.auth.ProjectInfo;

import com.fathomdb.cli.formatter.SimpleFormatter;
import com.fathomdb.cli.output.OutputSink;
import com.google.common.collect.Maps;

public class OpsProjectFormatter extends SimpleFormatter<ProjectInfo> {

	public OpsProjectFormatter() {
		super(ProjectInfo.class);
	}

	@Override
	public void visit(ProjectInfo o, OutputSink sink) throws IOException {
		LinkedHashMap<String, Object> values = Maps.newLinkedHashMap();

		values.put("name", o.getName());

		sink.outputRow(values);
	}
}
