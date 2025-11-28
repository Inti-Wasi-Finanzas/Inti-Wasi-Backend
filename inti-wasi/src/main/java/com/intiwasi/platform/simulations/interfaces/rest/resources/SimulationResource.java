package com.intiwasi.platform.simulations.interfaces.rest.resources;

import com.intiwasi.platform.simulations.domain.model.valueobjects.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SimulationResource(
        Long id,
        Long clientId,
        Long advisorId,
        ProgramaHabitacional programName,
        Currency currency,
        String fullName,
        String dni,
        LocalDate birthDate,
        String email,
        String phoneNumber,
        CivilStatus civilStatus,
        BigDecimal totalFamilyIncome,
        Boolean hasOtherProperty,
        Boolean receivedBonoBeforeFMV,
        TypeBond typeBond,
        String propertyName,
        BigDecimal propertyPrice,
        Boolean isPropertySustainable,
        FinancialInstitution financialInstitution,
        Integer deadlinesMonths,
        String typeRate,
        BigDecimal interestRate,
        Capitalization capitalization,
        BigDecimal downPaymentAmount,
        BigDecimal amountBond,
        BigDecimal amountFinanced,
        BigDecimal monthlyFee,
        BigDecimal van,
        BigDecimal tir,
        BigDecimal tcea,
        EstadoSimulacion estado,
        String rejectionReason,
        LocalDate simulationDate
) {}