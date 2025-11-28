package com.intiwasi.platform.simulations.domain.model.events;

import com.intiwasi.platform.simulations.domain.model.aggregates.Simulation;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class SimulationApprovedEvent extends ApplicationEvent {

    private final Simulation simulation;

    public SimulationApprovedEvent(Simulation simulation) {
        super(simulation);
        this.simulation = simulation;
    }

    public Simulation simulation() {
        return simulation;
    }
}