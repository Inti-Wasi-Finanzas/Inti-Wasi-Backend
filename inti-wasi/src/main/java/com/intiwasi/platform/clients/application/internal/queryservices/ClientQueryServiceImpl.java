package com.intiwasi.platform.clients.application.internal.queryservices;

import com.intiwasi.platform.clients.domain.model.aggregates.Client;
import com.intiwasi.platform.clients.domain.model.queries.GetAllClientsQuery;
import com.intiwasi.platform.clients.domain.model.queries.GetClientByUserIdQuery;
import com.intiwasi.platform.clients.domain.services.ClientQueryService;
import com.intiwasi.platform.clients.infrastructure.persistence.jpa.repositories.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientQueryServiceImpl implements ClientQueryService {

    private final ClientRepository clientRepository;

    public ClientQueryServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public Optional<Client> handle(GetClientByUserIdQuery query) {
        return clientRepository.findByUserId(query.userId());
    }

    @Override
    public List<Client> handle(GetAllClientsQuery query) {
        return clientRepository.findAll();
    }

}