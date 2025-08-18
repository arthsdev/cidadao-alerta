package com.artheus.cidadaoalerta.validation.validator;

import com.artheus.cidadaoalerta.validation.Longitude;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class LongitudeValidator implements ConstraintValidator<Longitude, Double> {

    @Override
    public boolean isValid(Double value, ConstraintValidatorContext context) {

        if (value == null) {
            return true; // aqui deixa @NotNull cuidar do null
        }

        return value >= -180 && value <= 180;
    }
}
