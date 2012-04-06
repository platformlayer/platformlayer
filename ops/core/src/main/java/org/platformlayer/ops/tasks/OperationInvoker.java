package org.platformlayer.ops.tasks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.BindingScope;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OperationType;
import org.platformlayer.ops.reflection.MethodInvoker;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class OperationInvoker {
    static final Logger log = Logger.getLogger(OperationInvoker.class);
    @Inject
    Provider<MethodInvoker> invokerProvider;

    public void invoke(Object controller) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        final BindingScope scope = BindingScope.get();
        Method method = findTargetMethod(controller, scope);
        if (method == null) {
            throw new IllegalStateException("Cannot find handler for operation on " + controller.getClass());
        }

        MethodInvoker invoker = invokerProvider.get();

        if (scope != null) {
            invoker.addProvider(new Function<Class<?>, Object>() {
                @Override
                public Object apply(Class<?> clazz) {
                    return scope.getInstance(clazz);
                }
            });
        }

        invoker.invokeMethod(controller, method);
    }

    private Method findTargetMethod(Object controller, BindingScope scope) {
        List<Method> candidates = Lists.newArrayList();

        for (Method method : controller.getClass().getMethods()) {
            if (!isCandidate(method)) {
                continue;
            }
            candidates.add(method);
        }

        if (candidates.isEmpty()) {
            return null;
        }

        Method best = null;

        for (Method candidate : candidates) {
            if (best == null) {
                best = candidate;
                continue;
            } else {
                best = findBetter(candidate, best, scope);
            }
        }

        return best;
    }

    private boolean isCandidate(Method method) {
        Handler handler = method.getAnnotation(Handler.class);
        if (handler != null)
            return true;

        int managedCount = 0;

        for (Class<?> parameterType : method.getParameterTypes()) {
            // if (parameterType.equals(TypedManagedItem.class)) {
            // managedCount++;
            // }

            if (parameterType.equals(ItemBase.class)) {
                managedCount++;
            }
        }

        // We require that we take at least one 'Managed<?>' parameter
        if (managedCount == 0) {
            return false;
        }

        return true;
    }

    private Method findBetter(Method l, Method r, BindingScope scope) {
        Handler lHandler = l.getAnnotation(Handler.class);
        Handler rHandler = r.getAnnotation(Handler.class);

        if (lHandler != null && rHandler == null) {
            return l;
        }

        if (rHandler != null && lHandler == null) {
            return r;
        }

        if (rHandler != null && lHandler != null) {
            OperationType operationType = scope.getInstance(OperationType.class);
            if (operationType != null) {
                List<OperationType> lOperations = getOperations(lHandler);
                List<OperationType> rOperations = getOperations(rHandler);

                boolean lContains = lOperations.contains(operationType);
                boolean rContains = rOperations.contains(operationType);

                if (lContains && !rContains) {
                    return l;
                }

                if (rContains && !lContains) {
                    return r;
                }

                // Treat empty as wildcards
                if (rOperations.isEmpty() && !lOperations.isEmpty()) {
                    return r;
                }

                // Treat empty as wildcards
                if (lOperations.isEmpty() && !rOperations.isEmpty()) {
                    return l;
                }
            } else {
                log.warn("No OperationType in scope");
            }
        }

        throw new IllegalArgumentException("Cannot compare " + l + " with " + r);
    }

    private List<OperationType> getOperations(Handler handler) {
        OperationType[] operations = handler.value();
        if (operations == null)
            return Lists.newArrayList();
        return Arrays.asList(operations);
    }

}
