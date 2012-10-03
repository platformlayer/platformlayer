package org.platformlayer.xaas.web;

import javax.validation.Configuration;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import com.google.inject.AbstractModule;

public class PlatformlayerValidationModule extends AbstractModule {

	@Override
	protected void configure() {
		Configuration<?> config = Validation.byDefaultProvider().configure();
		// config.messageInterpolator(new MyMessageInterpolator())
		// .traversableResolver( new MyTraversableResolver())
		// .constraintValidatorFactory(new MyConstraintValidatorFactory());

		config.messageInterpolator(new ValidationMessageInterpolator());

		ValidatorFactory factory = config.buildValidatorFactory();

		// ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		bind(Validator.class).toInstance(validator);
	}

}
