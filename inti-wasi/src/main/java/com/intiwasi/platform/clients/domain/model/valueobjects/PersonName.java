package com.intiwasi.platform.clients.domain.model.valueobjects;

public record PersonName(String value) {
    public PersonName {
        if (value == null || value.trim().length() < 5 || value.trim().length() > 100) {
            throw new IllegalArgumentException("Nombre completo debe tener entre 5 y 100 caracteres");
        }
    }

    public String fullName() {
        return value.trim();
    }
}