package org.platformlayer.ops;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;

public class CommandEnvironment {
    final Map<String, String> vars = Maps.newHashMap();

    public Set<Entry<String, String>> all() {
        return vars.entrySet();
    }

    public void add(String key, String value) {
        vars.put(key, value);
    }

    public CommandEnvironment deepCopy() {
        CommandEnvironment copy = new CommandEnvironment();
        copy.vars.putAll(this.vars);
        return copy;
    }

    public Set<String> keys() {
        return vars.keySet();
    }
}
