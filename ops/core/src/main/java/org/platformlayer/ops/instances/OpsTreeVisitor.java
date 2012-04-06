package org.platformlayer.ops.instances;

import java.util.List;

import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.strategies.Strategies;

public abstract class OpsTreeVisitor {
    public void visit(Object controller) throws OpsException {
        List<Object> children = Strategies.findChildren(controller);
        for (Object child : children) {
            visit(child);
        }
    }
}
