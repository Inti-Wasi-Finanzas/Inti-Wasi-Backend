package com.intiwasi.platform.clients.domain.services;

import com.intiwasi.platform.clients.domain.model.aggregates.Client;
import com.intiwasi.platform.clients.domain.model.queries.GetAllClientsQuery;
import com.intiwasi.platform.clients.domain.model.queries.GetClientByUserIdQuery;

import java.util.List;
import java.util.Optional;

public interface ClientQueryService {
    Optional<Client> handle(GetClientByUserIdQuery query);

    List<Client> handle(GetAllClientsQuery query);
}