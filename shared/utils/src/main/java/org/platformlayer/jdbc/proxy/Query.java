package org.platformlayer.jdbc.proxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Query {
	public static final String AUTOMATIC_INSERT = "__auto_insert__";
	public static final String AUTOMATIC_UPDATE = "__auto_update__";

	public String value();
}
