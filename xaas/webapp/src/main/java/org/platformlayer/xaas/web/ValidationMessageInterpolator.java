package org.platformlayer.xaas.web;

import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;

public class ValidationMessageInterpolator extends ResourceBundleMessageInterpolator {

	@Override
	public String interpolate(String message, Context context) {
		String s = super.interpolate(message, context);

		// Object annotation = context.getConstraintDescriptor().getAnnotation();
		// Map<String, Object> attributes = context.getConstraintDescriptor().getAttributes();
		// Set<Class<? extends Payload>> payload = context.getConstraintDescriptor().getPayload();

		return s;
	}

}