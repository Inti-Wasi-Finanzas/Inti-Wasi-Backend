package com.intiwasi.platform.clients.domain.model.commands;

import com.intiwasi.platform.clients.domain.model.valueobjects.Dni;
import com.intiwasi.platform.clients.domain.model.valueobjects.EmailAddress;
import com.intiwasi.platform.clients.domain.model.valueobjects.PersonName;
import com.intiwasi.platform.clients.domain.model.valueobjects.PhoneNumber;

import java.time.LocalDate;
import java.time.Month;

public record UpdateClientProfileCommand(
        Long userId,
        PersonName fullName,
        Dni dni,
        EmailAddress email,
        PhoneNumber phone,
        Double monthlyIncome
) {}