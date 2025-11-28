package com.intiwasi.platform.simulations.domain.model.commands;

public record RejectSimulationCommand(Long simulationId, Long advisorId, String reason) {}