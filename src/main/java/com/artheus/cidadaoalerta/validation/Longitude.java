package com.artheus.cidadaoalerta.validation;

import com.artheus.cidadaoalerta.validation.validator.LongitudeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = LongitudeValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Longitude {

    String message() default "Longitude deve estar entre -180 e 180";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
