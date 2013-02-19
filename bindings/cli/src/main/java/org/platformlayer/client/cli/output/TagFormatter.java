package org.platformlayer.client.cli.output;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.platformlayer.core.model.Tag;

import com.fathomdb.cli.CliContext;
import com.fathomdb.cli.formatter.SimpleFormatter;
import com.fathomdb.cli.output.OutputSink;
import com.google.common.collect.Maps;

public class TagFormatter extends SimpleFormatter<Tag> {

	public TagFormatter() {
		super(Tag.class);
	}

	@Override
	public void visit(CliContext context, Tag o, OutputSink sink) throws IOException {
		LinkedHashMap<String, Object> values = Maps.newLinkedHashMap();

		values.put("key", o.getKey());
		values.put("value", o.getValue());

		sink.outputRow(values);
	}
}
