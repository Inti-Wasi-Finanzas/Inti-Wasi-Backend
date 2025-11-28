package com.intiwasi.platform.clients.domain.model.valueobjects;

import java.util.regex.Pattern;

public record EmailAddress(String value) {
    private static final Pattern PATTERN = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

    public EmailAddress {
        if (value == null || !PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Formato de correo inválido");
        }
    }
}