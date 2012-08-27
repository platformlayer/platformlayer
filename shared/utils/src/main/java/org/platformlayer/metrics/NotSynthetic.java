package org.platformlayer.metrics;

import java.io.Serializable;
import java.lang.reflect.Method;

import com.google.inject.matcher.AbstractMatcher;

public class NotSynthetic extends AbstractMatcher<Object> implements Serializable {

	private static final long serialVersionUID = 1L;

	public NotSynthetic() {
	}

	@Override
	public boolean matches(Object other) {
		if (other instanceof Method) {
			Method method = (Method) other;
			return !method.isSynthetic();
		}

		return true;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof NotSynthetic;
	}

	@Override
	public int hashCode() {
		return NotSynthetic.class.hashCode();
	}

}