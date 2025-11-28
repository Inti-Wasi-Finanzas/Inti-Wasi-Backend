package com.intiwasi.platform.clients.domain.model.valueobjects;

public record PhoneNumber(String value) {
    public PhoneNumber {
        if (value == null || !value.matches("\\d{9}")) {
            throw new IllegalArgumentException("Teléfono debe tener 9 dígitos");
        }
    }
}