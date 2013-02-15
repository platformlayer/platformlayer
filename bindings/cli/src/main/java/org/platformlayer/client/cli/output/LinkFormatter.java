package org.platformlayer.client.cli.output;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.platformlayer.core.model.Link;

import com.fathomdb.cli.formatter.SimpleFormatter;
import com.fathomdb.cli.output.OutputSink;
import com.google.common.collect.Maps;

public class LinkFormatter extends SimpleFormatter<Link> {

	public LinkFormatter() {
		super(Link.class);
	}

	@Override
	public void visit(Link o, OutputSink sink) throws IOException {
		LinkedHashMap<String, Object> values = Maps.newLinkedHashMap();

		values.put("name", o.getName());
		values.put("target", o.getTarget().toString());

		sink.outputRow(values);
	}
}
