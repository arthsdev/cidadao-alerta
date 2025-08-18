package com.artheus.cidadaoalerta.validation.validator;

import com.artheus.cidadaoalerta.validation.Latitude;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class LatitudeValidator implements ConstraintValidator<Latitude, Double> {

    @Override
    public boolean isValid(Double value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // deixa @NotNull cuidar do null
        }
        return value >= -90 && value <= 90;
    }
}
