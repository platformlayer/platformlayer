package org.platformlayer.keystone.cli.formatters;

import java.io.IOException;
import java.util.LinkedHashMap;

import com.fathomdb.utils.Hex;
import org.platformlayer.auth.ServiceAccount;
import org.platformlayer.auth.ServiceAccountEntity;

import com.fathomdb.cli.formatter.SimpleFormatter;
import com.fathomdb.cli.output.OutputSink;
import com.google.common.collect.Maps;

public class ServiceAccountFormatter extends SimpleFormatter<ServiceAccount> {

	public ServiceAccountFormatter() {
		super(ServiceAccount.class);
	}

	@Override
	public void visit(ServiceAccount o, OutputSink sink) throws IOException {
		LinkedHashMap<String, Object> values = Maps.newLinkedHashMap();

		ServiceAccountEntity entity = (ServiceAccountEntity) o;

		values.put("subject", entity.subject);
		values.put("publicKeyData", Hex.toHex(entity.publicKeyData));

		sink.outputRow(values);
	}
}
