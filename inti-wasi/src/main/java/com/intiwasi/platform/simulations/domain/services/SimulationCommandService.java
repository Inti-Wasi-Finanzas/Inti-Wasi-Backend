package com.intiwasi.platform.simulations.domain.services;

import com.intiwasi.platform.simulations.domain.model.aggregates.Simulation;
import com.intiwasi.platform.simulations.domain.model.commands.*;

public interface SimulationCommandService {
    Simulation handle(CreateSimulationCommand command);
    Simulation handle(ApproveSimulationCommand command);
    Simulation handle(RejectSimulationCommand command);
    Simulation handle(UpdateSimulationCommand command);
    void handle(DeleteSimulationCommand command);
}