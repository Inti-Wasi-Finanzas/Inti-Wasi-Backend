package com.intiwasi.platform.simulations.interfaces.rest;


import com.intiwasi.platform.simulations.domain.model.aggregates.Simulation;
import com.intiwasi.platform.simulations.domain.model.commands.*;
import com.intiwasi.platform.simulations.domain.model.queries.GetPendingSimulationsForAdvisorQuery;
import com.intiwasi.platform.simulations.domain.model.queries.GetSimulationByIdQuery;
import com.intiwasi.platform.simulations.domain.model.queries.GetSimulationsSummaryByClientIdQuery;
import com.intiwasi.platform.simulations.domain.model.valueobjects.PaymentScheduleEntry;
import com.intiwasi.platform.simulations.domain.services.LoanCalculatorDomainService;
import com.intiwasi.platform.simulations.domain.services.SimulationCommandService;
import com.intiwasi.platform.simulations.domain.services.SimulationQueryService;
import com.intiwasi.platform.simulations.domain.services.pdf.PdfGenerationService;
import com.intiwasi.platform.simulations.interfaces.rest.resources.*;
import com.intiwasi.platform.simulations.interfaces.rest.transform.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.intiwasi.platform.simulations.interfaces.rest.transform.SimulationResourceFromEntityAssembler.toResource;

@RestController
@RequestMapping("/api/v1/simulations")
@Tag(name = "Simulations", description = "Gestión de las simulaciones del cliente")
public class SimulationsController {

    private final SimulationCommandService commandService;
    private final SimulationQueryService queryService;
    private final LoanCalculatorDomainService loanCalculatorDomainService;
    private final PdfGenerationService pdfGenerationService;

    public SimulationsController(SimulationCommandService commandService,
                                 SimulationQueryService queryService, LoanCalculatorDomainService loanCalculatorDomainService,
                                 PdfGenerationService pdfGenerationService) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.loanCalculatorDomainService = loanCalculatorDomainService;
        this.pdfGenerationService = pdfGenerationService;
    }

    @PostMapping
    public ResponseEntity<SimulationResource> create(@RequestBody CreateSimulationResource resource) {
        CreateSimulationCommand command = CreateSimulationCommandFromResourceAssembler.toCommand(resource);
        Simulation simulation = commandService.handle(command);
        return ResponseEntity.ok(toResource(simulation));
    }

    @PutMapping("/{simulationId}")
    public ResponseEntity<SimulationResource> update(@PathVariable Long simulationId,
                                                     @RequestBody UpdateSimulationResource resource) {
        UpdateSimulationCommand command = UpdateSimulationCommandFromResourceAssembler.toCommand(simulationId, resource);
        Simulation simulation = commandService.handle(command);
        return ResponseEntity.ok(toResource(simulation));
    }

    @DeleteMapping("/{simulationId}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long simulationId) {
        commandService.handle(new DeleteSimulationCommand(simulationId));
        return ResponseEntity.ok(Map.of(
                "Message", "Simulation with ID " + simulationId + " has been deleted successfully.",
                "Simulation Id deleted", String.valueOf(simulationId)
        ));
    }

    @PostMapping("/{simulationId}/approve")
    public ResponseEntity<SimulationResource> approve(@PathVariable Long simulationId,
                                                      @RequestBody ApproveSimulationResource resource) {
        ApproveSimulationCommand command = ApproveSimulationCommandFromResourceAssembler.toCommand(simulationId, resource);
        Simulation simulation = commandService.handle(command);
        return ResponseEntity.ok(toResource(simulation));
    }

    @PostMapping("/{simulationId}/reject")
    public ResponseEntity<SimulationResource> reject(@PathVariable Long simulationId,
                                                     @RequestBody ApproveSimulationResource resource,
                                                     @RequestParam String reason) {
        RejectSimulationCommand command = new RejectSimulationCommand(simulationId, resource.advisorId(), reason);
        Simulation simulation = commandService.handle(command);
        return ResponseEntity.ok(toResource(simulation));
    }

    @GetMapping("/{simulationId}")
    public ResponseEntity<SimulationResource> getById(@PathVariable Long simulationId) {
        return queryService.handle(new GetSimulationByIdQuery(simulationId))
                .map(sim -> ResponseEntity.ok(toResource(sim)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<SimulationResource>> getByClient(@PathVariable Long clientId) {
        var simulations = queryService.handle(new GetSimulationsSummaryByClientIdQuery(clientId));
        if (simulations.isEmpty()) return ResponseEntity.notFound().build();
        var resources = simulations.stream()
                .map(SimulationResourceFromEntityAssembler::toResource)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/advisor/{advisorId}/pending")
    public ResponseEntity<List<SimulationResource>> getPendingByAdvisor(@PathVariable Long advisorId) {
        var simulations = queryService.handle(new GetPendingSimulationsForAdvisorQuery(advisorId));
        var resources = simulations.stream()
                .map(SimulationResourceFromEntityAssembler::toResource)
                .toList();
        return ResponseEntity.ok(resources);
    }

    // --- Cronograma en JSON ---
    @GetMapping("/{simulationId}/schedule")
    public ResponseEntity<List<PaymentScheduleEntryResource>> getSchedule(@PathVariable Long simulationId) {
        Simulation simulation = queryService.handle(new GetSimulationByIdQuery(simulationId))
                .orElseThrow(() -> new IllegalArgumentException("Simulation not found"));

        List<PaymentScheduleEntry> schedule = loanCalculatorDomainService.generateSchedule(simulation);

        var resources = schedule.stream()
                .map(PaymentScheduleEntryResourceFromValueObjectAssembler::toResource)
                .toList();

        return ResponseEntity.ok(resources);
    }

    // --- Cronograma en PDF ---
    @GetMapping("/{simulationId}/schedule/pdf")
    public ResponseEntity<byte[]> getPaymentSchedulePdf(@PathVariable Long simulationId) {
        Simulation simulation = queryService.handle(new GetSimulationByIdQuery(simulationId))
                .orElseThrow(() -> new IllegalArgumentException("Simulation not found"));
        byte[] pdf = pdfGenerationService.generatePaymentSchedulePdf(simulation);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cronograma-" + simulationId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
