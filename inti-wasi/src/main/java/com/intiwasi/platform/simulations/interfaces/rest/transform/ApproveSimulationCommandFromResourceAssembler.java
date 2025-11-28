package com.intiwasi.platform.simulations.interfaces.rest.transform;

import com.intiwasi.platform.simulations.domain.model.commands.ApproveSimulationCommand;
import com.intiwasi.platform.simulations.interfaces.rest.resources.ApproveSimulationResource;

public class ApproveSimulationCommandFromResourceAssembler {

    public static ApproveSimulationCommand toCommand(Long simulationId, ApproveSimulationResource resource) {
        return new ApproveSimulationCommand(simulationId, resource.advisorId());
    }
}