package org.platformlayer.ops;

import java.util.List;

public interface OpsTree {
    List<Object> getChildren() throws OpsException;
}
