package com.intiwasi.platform.clients.domain.model.valueobjects;

public record Dni(String value) {
    public Dni {
        if (value == null || !value.matches("\\d{8}")) {
            throw new IllegalArgumentException("DNI debe tener exactamente 8 dígitos");
        }
    }
}