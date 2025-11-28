package com.intiwasi.platform.simulations.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentScheduleEntryResource(
        int installmentNumber,
        LocalDate dueDate,
        BigDecimal teaAnnual,
        BigDecimal tepPeriod,
        BigDecimal beginningBalance,
        BigDecimal interest,
        BigDecimal installmentWithSegDes,
        BigDecimal amortization,
        BigDecimal seguroDesgravamen,
        BigDecimal seguroRiesgo,
        BigDecimal comision,
        BigDecimal endingBalance,
        BigDecimal flujo
) { }
