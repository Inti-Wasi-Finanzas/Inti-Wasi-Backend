package com.intiwasi.platform.clients.interfaces.rest.resources;

public record ClientResource(
        Long id,
        String fullName,
        String dni,
        String email,
        String phone,
        Double monthlyIncome

        //LocalDate birthDate,
        //String civilStatus,
        //String district,
        //Integer dependents,
        //String currency,
        //Double totalFamilyIncome,
        //String desiredSubsidy
) {}