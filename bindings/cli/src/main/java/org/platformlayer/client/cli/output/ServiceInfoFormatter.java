package org.platformlayer.client.cli.output;

import java.io.IOException;
import java.util.LinkedHashMap;

import com.fathomdb.cli.formatter.SimpleFormatter;
import com.fathomdb.cli.output.OutputSink;
import org.platformlayer.core.model.ServiceInfo;

import com.google.common.collect.Maps;

public class ServiceInfoFormatter extends SimpleFormatter<ServiceInfo> {

    public ServiceInfoFormatter() {
        super(ServiceInfo.class);
    }

    @Override
    public void visit(ServiceInfo o, OutputSink sink) throws IOException {
        LinkedHashMap<String, Object> values = Maps.newLinkedHashMap();

        values.put("serviceType", o.getServiceType());
        values.put("namespace", o.getNamespace());
        values.put("description", o.getDescription());
        values.put("publicTypes", o.getPublicTypes());
        values.put("adminTypes", o.getAdminTypes());

        sink.outputRow(values);
    }

}
