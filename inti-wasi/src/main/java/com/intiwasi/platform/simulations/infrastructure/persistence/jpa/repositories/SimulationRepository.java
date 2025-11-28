package com.intiwasi.platform.simulations.infrastructure.persistence.jpa.repositories;

import com.intiwasi.platform.simulations.domain.model.aggregates.Simulation;
import com.intiwasi.platform.simulations.domain.model.valueobjects.EstadoSimulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SimulationRepository extends JpaRepository<Simulation, Long> {
    //List<Simulation> findByClient_Id(Long clientId);
    //List<Simulation> findByClient_UserId(Long userId);
    List<Simulation> findByClientId(Long clientId);
    List<Simulation> findByAdvisorIdAndEstado(Long advisorId, EstadoSimulacion estado);
}