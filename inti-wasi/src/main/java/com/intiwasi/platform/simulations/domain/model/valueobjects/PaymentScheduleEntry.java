package com.intiwasi.platform.simulations.domain.model.valueobjects;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entrada del cronograma de pagos método francés (30 días).
 */
public record PaymentScheduleEntry(
        int installmentNumber,           // Nº de cuota
        LocalDate dueDate,               // Fecha de vencimiento
        BigDecimal teaAnnual,            // TEA anual (%)
        BigDecimal tepPeriod,            // TEP mensual (%)
        BigDecimal beginningBalance,     // Saldo inicial
        BigDecimal interest,             // Interés
        BigDecimal installmentWithSegDes,// Cuota (incluye seguro desgravamen)
        BigDecimal amortization,         // Amortización
        BigDecimal seguroDesgravamen,    // Seguro de desgravamen
        BigDecimal seguroRiesgo,         // Seguro de riesgo / inmueble
        BigDecimal comision,             // Comisión
        BigDecimal endingBalance,        // Saldo final
        BigDecimal flujo                 // Flujo total del periodo
) { }

/*
public record PaymentScheduleEntry( // Entrada de cronograma de pagos
        int currentPeriod, // Número de cuota
        LocalDate dueDate, // Fecha de vencimiento
        BigDecimal startingBalance, // Saldo inicial
        BigDecimal interest, // Saldo inicial
        BigDecimal principalPayment, // Cuota total (capital + interes + comisiones + seguros)
        BigDecimal insurance, // Interés
        BigDecimal comission, // Amortización
        BigDecimal totalPayment, // Pago total
        BigDecimal balance // Saldo final

        int installmentNumber, // Número de cuota
        LocalDate dueDate, // Fecha de vencimiento
        BigDecimal beginningBalance, // Saldo inicial
        BigDecimal principal, // Saldo inicial
        BigDecimal installment, // Cuota total (capital + interes + comisiones + seguros)
        BigDecimal interest, // Interés
        BigDecimal amortization, // Amortización
        BigDecimal commissions, // Comisión
        BigDecimal totalPayment, // Pago total
        BigDecimal endingBalance // Saldo final

) { }*/