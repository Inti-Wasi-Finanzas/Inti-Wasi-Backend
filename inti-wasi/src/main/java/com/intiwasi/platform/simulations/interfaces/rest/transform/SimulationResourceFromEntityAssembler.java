package com.intiwasi.platform.simulations.interfaces.rest.transform;

import com.intiwasi.platform.simulations.domain.model.aggregates.Simulation;
import com.intiwasi.platform.simulations.interfaces.rest.resources.SimulationResource;

public class SimulationResourceFromEntityAssembler {

    public static SimulationResource toResource(Simulation simulation) {
        return new SimulationResource(
                simulation.getId(),
                simulation.getClientId(),
                simulation.getAdvisorId(),
                simulation.getProgramName(),
                simulation.getCurrency(),
                simulation.getFullName(),
                simulation.getDni(),
                simulation.getBirthDate(),
                simulation.getEmail(),
                simulation.getPhoneNumber(),
                simulation.getCivilStatus(),
                simulation.getTotalFamilyIncome(),
                simulation.getHasOtherProperty(),
                simulation.getReceivedBonoBeforeFMV(),
                simulation.getTypeBond(),
                simulation.getPropertyName(),
                simulation.getPropertyPrice(),
                simulation.getIsPropertySustainable(),
                simulation.getFinancialInstitution(),
                simulation.getDeadlinesMonths(),
                simulation.getTypeRate(),
                simulation.getInterestRate(),
                simulation.getCapitalization(),
                simulation.getDownPayment(),
                simulation.getAmountBond(),
                simulation.getAmountFinanced(),
                simulation.getMonthlyFee(),
                simulation.getVan(),
                simulation.getTir(),
                simulation.getTcea(),
                simulation.getEstado(),
                simulation.getRejectionReason(),
                simulation.getSimulationDate()
        );
    }
}