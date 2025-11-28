package com.intiwasi.platform.clients.interfaces.rest;

import com.intiwasi.platform.clients.domain.model.queries.GetAllClientsQuery;
import com.intiwasi.platform.clients.domain.model.queries.GetClientByUserIdQuery;
import com.intiwasi.platform.clients.domain.services.ClientCommandService;
import com.intiwasi.platform.clients.domain.services.ClientQueryService;
import com.intiwasi.platform.clients.interfaces.rest.resources.ClientResource;
import com.intiwasi.platform.clients.interfaces.rest.resources.UpdateClientResource;
import com.intiwasi.platform.clients.interfaces.rest.transform.ClientResourceFromEntityAssembler;
import com.intiwasi.platform.clients.interfaces.rest.transform.UpdateClientCommandFromResourceAssembler;
import com.intiwasi.platform.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clients")
@Tag(name = "Clients", description = "Gestión del perfil del cliente")
public class ClientsController {

    private final ClientCommandService clientCommandService;
    private final ClientQueryService clientQueryService;

    public ClientsController(ClientCommandService clientCommandService, ClientQueryService clientQueryService) {
        this.clientCommandService = clientCommandService;
        this.clientQueryService = clientQueryService;
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Obtener mi perfil", description = "Devuelve el perfil del cliente autenticado")
    public ResponseEntity<ClientResource> getMyProfile(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        var client = clientQueryService.handle(new GetClientByUserIdQuery(userId))
                .orElseThrow(() -> new RuntimeException("Perfil de cliente no encontrado"));

        var resource = ClientResourceFromEntityAssembler.toResourceFromEntity(client);
        return ResponseEntity.ok(resource);
    }

    @GetMapping("/allProfiles")
    @PreAuthorize("hasRole('ADVISOR')")
    @Operation(summary = "Obtener todos los perfiles", description = "Devuelve todos los datos de los clientes autenticados con perfiles creados para la vista del asesor")
    public ResponseEntity<List<ClientResource>> getAllClients(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        var clients = clientQueryService.handle(new GetAllClientsQuery(userId));
        if (clients.isEmpty()) return ResponseEntity.notFound().build();
        var clientResources = clients.stream()
                .map(ClientResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(clientResources);
    }


    @PutMapping("/profile")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Actualizar mi perfil", description = "Actualiza los datos del cliente autenticado")
    public ResponseEntity<ClientResource> updateProfile(
            @Valid @RequestBody UpdateClientResource resource,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);
        var command = UpdateClientCommandFromResourceAssembler.toCommandFromResource(resource, userId);
        var updatedClient = clientCommandService.handle(command);

        var updatedResource = ClientResourceFromEntityAssembler.toResourceFromEntity(updatedClient);
        return ResponseEntity.ok(updatedResource);
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        var userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }
}