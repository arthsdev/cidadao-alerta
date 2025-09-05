package com.artheus.cidadaoalerta.unit.validation;

import com.artheus.cidadaoalerta.validation.validator.LatitudeValidator;
import com.artheus.cidadaoalerta.validation.validator.LongitudeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoordenadasValidatorTest {

    private LatitudeValidator latitudeValidator;
    private LongitudeValidator longitudeValidator;

    @BeforeEach
    void setup() {
        latitudeValidator = new LatitudeValidator();
        longitudeValidator = new LongitudeValidator();
    }

    // ================= LATITUDE =================
    @Test
    void deveValidarLatitudesValidas() {
        assertTrue(latitudeValidator.isValid(0.0, null));
        assertTrue(latitudeValidator.isValid(-90.0, null));
        assertTrue(latitudeValidator.isValid(90.0, null));
        assertTrue(latitudeValidator.isValid(45.5, null));
    }

    @Test
    void deveInvalidarLatitudesInvalidas() {
        assertFalse(latitudeValidator.isValid(-90.1, null));
        assertFalse(latitudeValidator.isValid(90.1, null));
        assertFalse(latitudeValidator.isValid(100.0, null));
        assertFalse(latitudeValidator.isValid(-100.0, null));
    }

    @Test
    void deveValidarLatitudeNula() {
        assertTrue(latitudeValidator.isValid(null, null));
    }

    // ================= LONGITUDE =================
    @Test
    void deveValidarLongitudesValidas() {
        assertTrue(longitudeValidator.isValid(0.0, null));
        assertTrue(longitudeValidator.isValid(-180.0, null));
        assertTrue(longitudeValidator.isValid(180.0, null));
        assertTrue(longitudeValidator.isValid(90.0, null));
        assertTrue(longitudeValidator.isValid(-90.0, null));
    }

    @Test
    void deveInvalidarLongitudesInvalidas() {
        assertFalse(longitudeValidator.isValid(-180.1, null));
        assertFalse(longitudeValidator.isValid(180.1, null));
        assertFalse(longitudeValidator.isValid(200.0, null));
        assertFalse(longitudeValidator.isValid(-200.0, null));
    }

    @Test
    void deveValidarLongitudeNula() {
        assertTrue(longitudeValidator.isValid(null, null));
    }
}
