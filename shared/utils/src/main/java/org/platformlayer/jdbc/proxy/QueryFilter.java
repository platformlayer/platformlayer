package org.platformlayer.jdbc.proxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface QueryFilter {
	public final static String LIMIT = ";;limit;;";

	// TODO: It would be nice to infer the filter from the parameter name
	// e.g. @QueryFilter Integer limit => LIMIT ?
	// e.g. @QueryFilter String col1 => col1 = ?
	// BUT that relies on parameter names being always available at runtime. Maybe in Java 8.
	public String value();
}
