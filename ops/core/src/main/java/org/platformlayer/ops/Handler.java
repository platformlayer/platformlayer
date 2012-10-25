package org.platformlayer.ops;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.platformlayer.core.model.Action;

@Retention(RetentionPolicy.RUNTIME)
public @interface Handler {
	Class<? extends Action>[] value() default {};
}
