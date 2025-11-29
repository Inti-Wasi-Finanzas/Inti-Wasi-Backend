package com.intiwasi.platform.clients.interfaces.rest.resources;

public record ClientResource(
        Long id,
        String fullName,
        String dni,
        String email,
        String phone,
        Double monthlyIncome

) {}