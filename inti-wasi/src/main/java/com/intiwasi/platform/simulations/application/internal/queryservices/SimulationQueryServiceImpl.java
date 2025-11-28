package com.intiwasi.platform.simulations.application.internal.queryservices;

import com.intiwasi.platform.simulations.domain.model.aggregates.Simulation;
import com.intiwasi.platform.simulations.domain.model.queries.GetPendingSimulationsForAdvisorQuery;
import com.intiwasi.platform.simulations.domain.model.queries.GetSimulationByIdQuery;
import com.intiwasi.platform.simulations.domain.model.queries.GetSimulationsSummaryByClientIdQuery;
import com.intiwasi.platform.simulations.domain.model.valueobjects.EstadoSimulacion;
import com.intiwasi.platform.simulations.domain.services.SimulationQueryService;
import com.intiwasi.platform.simulations.infrastructure.persistence.jpa.repositories.SimulationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SimulationQueryServiceImpl implements SimulationQueryService {

    private final SimulationRepository repository;

    @Override
    public List<Simulation> handle(GetSimulationsSummaryByClientIdQuery query) {
        return repository.findByClientId(query.clientId());
    }

    @Override
    public Optional<Simulation> handle(GetSimulationByIdQuery query) {
        return repository.findById(query.simulationId());
    }

    @Override
    public List<Simulation> handle(GetPendingSimulationsForAdvisorQuery query) {
        return repository.findByAdvisorIdAndEstado(query.advisorId(), EstadoSimulacion.PENDIENTE);
    }
}