package org.platformlayer.client.cli.commands;

import java.util.Map;
import java.util.Map.Entry;

import org.python.core.PyDictionary;
import org.python.core.PyList;

public class JythonToJson {
    final StringBuilder sb = new StringBuilder();

    public String getJson() {
        return sb.toString();
    }

    public void append(Object item) {
        if (item instanceof PyDictionary) {
            append((PyDictionary) item);
        } else if (item instanceof PyList) {
            append((PyList) item);
        } else if (item instanceof String) {
            sb.append("\"");
            sb.append(item.toString());
            sb.append("\"");
        } else {
            throw new IllegalArgumentException("Unhandled python type: " + item.getClass());
        }
    }

    private void append(PyDictionary dict) {
        sb.append("{ ");

        int count = 0;

        for (Object entryObj : dict.entrySet()) {
            if (count != 0)
                sb.append(",");
            count++;

            Map.Entry entry = (Entry) entryObj;
            Object key = entry.getKey();
            Object value = entry.getValue();

            sb.append("\"");
            sb.append(key.toString());
            sb.append("\": ");

            append(value);
        }
        sb.append(" }");
    }

    private void append(PyList dict) {
        sb.append("[ ");

        int count = 0;

        for (Object item : dict) {
            if (count != 0)
                sb.append(",");
            count++;

            append(item);
        }
        sb.append(" ]");
    }
}
