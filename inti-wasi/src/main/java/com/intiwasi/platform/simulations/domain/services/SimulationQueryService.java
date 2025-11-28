package com.intiwasi.platform.simulations.domain.services;

import com.intiwasi.platform.simulations.domain.model.aggregates.Simulation;
import com.intiwasi.platform.simulations.domain.model.queries.*;
        import java.util.List;
import java.util.Optional;

public interface SimulationQueryService {
    Optional<Simulation> handle(GetSimulationByIdQuery query);
    List<Simulation> handle(GetSimulationsSummaryByClientIdQuery query);
    List<Simulation> handle(GetPendingSimulationsForAdvisorQuery query);
}