package org.platformlayer.ops;

import java.util.List;

public class OperationRecursor {

    public static void doRecurseOperation(OpsContext opsContext, Object target) throws OpsException {
        if (target instanceof CustomRecursor) {
            CustomRecursor customRecursor = (CustomRecursor) target;
            customRecursor.doRecurseOperation();
            return;
        }

        doRecurseChildren(opsContext, target);
    }

    public static void doRecurseChildren(OpsContext opsContext, Object target) throws OpsException {
        if (target instanceof OpsTree) {
            List<Object> children = ((OpsTree) target).getChildren();
            if (children != null) {
                opsContext.recurseOperation(children);
            }
        }
    }

}
