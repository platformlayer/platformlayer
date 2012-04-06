package org.platformlayer.ops.metrics.collectd;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.platformlayer.CastUtils;
import org.platformlayer.ops.BindingScope;
import org.platformlayer.ops.CustomRecursor;
import org.platformlayer.ops.OperationRecursor;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTree;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class OpsTreeBase implements OpsTree, CustomRecursor {
    private List<Object> children = null;

    @Override
    public List<Object> getChildren() throws OpsException {
        if (children == null) {
            children = Lists.newArrayList();
            addChildren();
        }
        return children;
    }

    public <T> T addChild(T child) throws OpsException {
        getChildren().add(child);
        return child;
    }

    public void addChildren(Collection<?> children) throws OpsException {
        getChildren().addAll(children);
    }

    protected <T> T getChild(Class<T> findClass) throws OpsException {
        for (Object child : getChildren()) {
            if (findClass.isInstance(child)) {
                return CastUtils.as(child, findClass);
            }
        }
        throw new OpsException("Could not find child matching type: " + findClass);
    }

    public static <T> T injected(Class<T> clazz) {
        return OpsContext.get().getInjector().getInstance(clazz);
    }

    protected abstract void addChildren() throws OpsException;

    Map<Class<?>, Object> childScope;

    protected <T> T pushChildScope(Class<T> clazz, T o) {
        if (childScope == null) {
            childScope = Maps.newHashMap();
        }
        childScope.put(clazz, o);
        return o;
    }

    protected <T> T pushChildScope(T o) {
        if (o == null)
            throw new IllegalArgumentException();
        Class<T> clazz = (Class<T>) o.getClass();
        return pushChildScope(clazz, o);
    }

    @Override
    public void doRecurseOperation() throws OpsException {
        // TODO: Clear child scope???
        BindingScope scope = null;

        try {
            if (childScope != null && !childScope.isEmpty()) {
                scope = BindingScope.push(childScope.values());
            }
            OpsContext opsContext = OpsContext.get();
            OperationRecursor.doRecurseChildren(opsContext, this);
        } finally {
            if (scope != null) {
                scope.pop();
            }
        }
    }
}
