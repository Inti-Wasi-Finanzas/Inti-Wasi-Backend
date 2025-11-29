package com.intiwasi.platform.clients.interfaces.rest.transform;

import com.intiwasi.platform.clients.domain.model.commands.UpdateClientProfileCommand;
import com.intiwasi.platform.clients.domain.model.valueobjects.Dni;
import com.intiwasi.platform.clients.domain.model.valueobjects.EmailAddress;
import com.intiwasi.platform.clients.domain.model.valueobjects.PersonName;
import com.intiwasi.platform.clients.domain.model.valueobjects.PhoneNumber;
import com.intiwasi.platform.clients.interfaces.rest.resources.UpdateClientResource;

public class UpdateClientCommandFromResourceAssembler {

    public static UpdateClientProfileCommand toCommandFromResource(UpdateClientResource resource, Long userId) {
        return new UpdateClientProfileCommand(
                userId,
                new PersonName(resource.fullName()),
                new Dni(resource.dni()),
                new EmailAddress(resource.email()),
                new PhoneNumber(resource.phone()),
                resource.monthlyIncome()
        );
    }
}