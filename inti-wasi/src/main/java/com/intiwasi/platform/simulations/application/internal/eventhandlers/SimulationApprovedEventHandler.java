package com.intiwasi.platform.simulations.application.internal.eventhandlers;

import com.intiwasi.platform.simulations.domain.model.aggregates.Simulation;
import com.intiwasi.platform.simulations.domain.model.events.SimulationApprovedEvent;
//import com.intiwasi.platform.simulations.domain.services.pdf.PdfGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SimulationApprovedEventHandler {

    public record SimulationApprovedEvent(Simulation simulation) {}

    @EventListener
    public void onSimulationApproved(SimulationApprovedEvent event) {
        // TODO: notificar por email, etc.
        System.out.println("Simulation approved: " + event.simulation().getId());
    }

    /*private final PdfGenerationService pdfGenerationService;

    @EventListener
    public void handle(SimulationApprovedEvent event) {
        Simulation simulation = event.simulation();
        try {
            pdfGenerationService.generatePaymentSchedulePdf(simulation);
        } catch (Exception e) {
            // Loggear error
            e.printStackTrace();
        }
    }*/
}