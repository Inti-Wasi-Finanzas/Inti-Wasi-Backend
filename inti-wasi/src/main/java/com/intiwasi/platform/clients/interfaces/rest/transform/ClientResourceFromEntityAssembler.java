package com.intiwasi.platform.clients.interfaces.rest.transform;

import com.intiwasi.platform.clients.domain.model.aggregates.Client;
import com.intiwasi.platform.clients.interfaces.rest.resources.ClientResource;

public class ClientResourceFromEntityAssembler {

    public static ClientResource toResourceFromEntity(Client client) {
        if (client == null) return null;

        return new ClientResource(
                client.getId(),
                client.getFullName() != null ? client.getFullName().fullName() : null,
                client.getDni() != null ? client.getDni().value() : null,
                client.getEmail() != null ? client.getEmail().value() : null,
                client.getPhone() != null ? client.getPhone().value() : null,
                client.getMonthlyIncome()
        );
    }
}