package com.example.trial.weather.domain;

import com.openpojo.reflection.filters.FilterPackageInfo;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.rule.impl.NoFieldShadowingRule;
import com.openpojo.validation.rule.impl.NoPublicFieldsExceptStaticFinalRule;
import com.openpojo.validation.rule.impl.SetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import org.junit.Test;

/**
 * @author ignaciolarranaga@gmail.com
 */
public class POJOTest {

	// The package to test
	private static final String POJO_PACKAGE = "com.example.trial.weather.domain";
	
	@Test
	public void validate() {
		Validator validator = ValidatorBuilder.create()
            // Add Rules to validate structure for POJO_PACKAGE
            // See com.openpojo.validation.rule.impl for more ...
            .with(new GetterMustExistRule())
            .with(new SetterMustExistRule())
            .with(new NoPublicFieldsExceptStaticFinalRule())
            .with(new NoFieldShadowingRule())
            /// Add Testers to validate behaviour for POJO_PACKAGE
            // See com.openpojo.validation.test.impl for more ...
            .with(new SetterTester())
            .with(new GetterTester())
            .build();

        // Executing the validation
        validator.validate(POJO_PACKAGE, new FilterPackageInfo());
	}
	
}