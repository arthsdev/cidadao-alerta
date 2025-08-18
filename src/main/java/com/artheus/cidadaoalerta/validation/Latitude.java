package com.artheus.cidadaoalerta.validation;

import com.artheus.cidadaoalerta.validation.validator.LatitudeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = LatitudeValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Latitude {

    String message() default "Latitude deve estar entre -90 e 90";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
