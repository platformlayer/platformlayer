package org.platformlayer.ops;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Handler {
    OperationType[] value() default {};
}
