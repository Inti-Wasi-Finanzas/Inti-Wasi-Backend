package com.intiwasi.platform.simulations.domain.services;

import com.intiwasi.platform.simulations.domain.model.aggregates.Simulation;
import com.intiwasi.platform.simulations.domain.model.valueobjects.PaymentScheduleEntry;

import java.util.List;

/**
 * Servicio de dominio para cálculos financieros de la simulación:
 * bono, monto financiado, cronograma, VAN, TIR, TCEA, etc.
 */
public interface LoanCalculatorDomainService {

    /**
     * Calcula y setea en la entidad Simulation:
     * - Bono
     * - Monto financiado
     * - Cronograma (para obtener cuota fija)
     * - Cuota fija mensual
     * - VAN, TIR y TCEA
     */
    void enrichSimulation(Simulation simulation);

    /**
     * Genera el cronograma de pagos método francés (30 días)
     * a partir de los datos de la simulación.
     */
    List<PaymentScheduleEntry> generateSchedule(Simulation simulation);
}