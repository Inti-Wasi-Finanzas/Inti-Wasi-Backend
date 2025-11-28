package com.intiwasi.platform.clients.domain.services;

import com.intiwasi.platform.clients.domain.model.aggregates.Client;
import com.intiwasi.platform.clients.domain.model.commands.UpdateClientProfileCommand;

public interface ClientCommandService {
    Client handle(UpdateClientProfileCommand command);
}