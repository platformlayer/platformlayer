package org.platformlayer.xaas.web;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import com.google.inject.AbstractModule;

public class PlatformlayerValidationModule extends AbstractModule {

	@Override
	protected void configure() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		// Set<ConstraintViolation<MyBean>> constraintViolations =
		// validator.validate(bean);
		bind(Validator.class).toInstance(validator);
	}

}
