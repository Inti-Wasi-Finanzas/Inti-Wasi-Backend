package com.intiwasi.platform.clients.interfaces.rest.resources;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
public record UpdateClientResource(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 5, max = 100)
        String fullName,

        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(regexp = "\\d{8}", message = "DNI debe tener 8 dígitos")
        String dni,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "\\d{9}", message = "Teléfono debe tener 9 dígitos")
        String phone,

        @NotNull(message = "Ingreso mensual obligatorio")
        @Positive(message = "Ingreso debe ser mayor a 0")
        Double monthlyIncome
        /*
        LocalDate birthDate,

        String civilStatus,

        String district,

        Integer dependents,

        @NotBlank(message = "La moneda es obligatoria")
        String currency, // "SOLES" o "DOLARES"

        String jobType,

        Integer jobMonths,

        String incomeProof,

        @NotNull(message = "Ingreso mensual obligatorio")
        @Positive(message = "Ingreso debe ser mayor a 0")
        Double monthlyIncome,

        @PositiveOrZero
        Double spouseIncome,

        Boolean firstHome,

        Boolean hasOtherProperty,

        Boolean receivedBonoBefore,

        String desiredSubsidy */
) {}