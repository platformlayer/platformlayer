package org.platformlayer.client.cli.output;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.platformlayer.client.cli.PlatformLayerCliContext;
import org.platformlayer.core.model.PlatformLayerKey;

import com.fathomdb.cli.CliContext;
import com.fathomdb.cli.formatter.SimpleFormatter;
import com.fathomdb.cli.output.OutputSink;
import com.google.common.collect.Maps;

public class PlatformLayerKeyFormatter extends SimpleFormatter<PlatformLayerKey> {

	public PlatformLayerKeyFormatter() {
		super(PlatformLayerKey.class);
	}

	@Override
	public void visit(CliContext context, PlatformLayerKey o, OutputSink sink) throws IOException {

		String s = null;

		if (o != null) {
			s = Utils.formatUrl((PlatformLayerCliContext) context, o);
		}

		LinkedHashMap<String, Object> values = Maps.newLinkedHashMap();

		values.put("key", s);

		sink.outputRow(values);
	}
}
