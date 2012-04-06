package org.platformlayer.xml;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import com.google.common.collect.Maps;

public class MemorySchemaOutputResolver extends SchemaOutputResolver {
    final Map<String, StringWriter> writers = Maps.newHashMap();

    @Override
    public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
        String key = namespaceUri;

        StringWriter writer;
        if (writers.containsKey(key)) {
            throw new IllegalStateException();
        } else {
            writer = new StringWriter();
            writers.put(key, writer);
        }
        StreamResult result = new StreamResult(writer);
        result.setSystemId(suggestedFileName);
        return result;
    }

    public Map<String, StringWriter> getWriters() {
        return writers;
    }
}
