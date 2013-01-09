package org.platformlayer.ops.tasks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.*;
import org.platformlayer.core.model.Action;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.BindingScope;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.reflection.MethodInvoker;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class OperationInvoker {
	static final Logger log = LoggerFactory.getLogger(OperationInvoker.class);
	@Inject
	Provider<MethodInvoker> invokerProvider;

	public void invoke(Object controller) throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
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
			if (!isCandidate(method, scope)) {
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

	private boolean isCandidate(Method method, BindingScope scope) {
		Action action = scope.getInstance(Action.class);

		Handler handler = method.getAnnotation(Handler.class);
		if (handler != null) {
			if (!canHandleAction(handler, action, true)) {
				return false;
			}

			return true;
		}

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

	private boolean canHandleAction(Handler handler, Action action, boolean useWildcard) {
		Class<? extends Action>[] operations = handler.value();
		if (operations == null || operations.length == 0) {
			return useWildcard;
		}

		for (Class<? extends Action> actionClass : operations) {
			if (actionClass.isInstance(action)) {
				return true;
			}
		}
		return false;
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
			Action action = scope.getInstance(Action.class);
			if (action != null) {
				boolean lExplicit = canHandleAction(lHandler, action, false);
				boolean rExplicit = canHandleAction(rHandler, action, false);

				if (lExplicit && !rExplicit) {
					return l;
				}

				if (rExplicit && !lExplicit) {
					return r;
				}

				// TODO: How do we get here??
				boolean lWildcard = canHandleAction(lHandler, action, true);
				boolean rWildcard = canHandleAction(lHandler, action, true);

				if (lWildcard && !rWildcard) {
					return l;
				}

				if (rWildcard && !lWildcard) {
					return r;
				}
			} else {
				log.warn("No OperationType in scope");
			}
		}

		throw new IllegalArgumentException("Cannot compare " + l + " with " + r);
	}

	// private List<Class<? extends Action>> getOperations(Handler handler) {
	// Class<? extends Action>[] operations = handler.value();
	// if (operations == null) {
	// return Lists.newArrayList();
	// }
	// return Arrays.asList(operations);
	// }

}
