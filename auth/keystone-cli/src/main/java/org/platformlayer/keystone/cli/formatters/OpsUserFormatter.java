package org.platformlayer.keystone.cli.formatters;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.platformlayer.auth.OpsUser;

import com.fathomdb.cli.formatter.SimpleFormatter;
import com.fathomdb.cli.output.OutputSink;
import com.google.common.collect.Maps;

public class OpsUserFormatter extends SimpleFormatter<OpsUser> {

	public OpsUserFormatter() {
		super(OpsUser.class);
	}

	@Override
	public void visit(OpsUser o, OutputSink sink) throws IOException {
		LinkedHashMap<String, Object> values = Maps.newLinkedHashMap();

		// values.put("id", o.id);
		values.put("user", o.toString());

		sink.outputRow(values);
	}
}
