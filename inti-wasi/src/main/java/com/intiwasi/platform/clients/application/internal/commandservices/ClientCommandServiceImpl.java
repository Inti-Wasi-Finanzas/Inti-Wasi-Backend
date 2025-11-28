package com.intiwasi.platform.clients.application.internal.commandservices;

import com.intiwasi.platform.clients.domain.model.aggregates.Client;
import com.intiwasi.platform.clients.domain.model.commands.UpdateClientProfileCommand;
import com.intiwasi.platform.clients.domain.services.ClientCommandService;
import com.intiwasi.platform.clients.infrastructure.persistence.jpa.repositories.ClientRepository;
import org.springframework.stereotype.Service;

@Service
public class ClientCommandServiceImpl implements ClientCommandService {

    private final ClientRepository clientRepository;

    public ClientCommandServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public Client handle(UpdateClientProfileCommand command) {
        Client client = clientRepository.findByUserId(command.userId())
                .orElseGet(Client::new);

        client.setUserId(command.userId());
        client.setFullName(command.fullName());
        client.setDni(command.dni());
        client.setEmail(command.email());
        client.setPhone(command.phone());
        client.setMonthlyIncome(command.monthlyIncome());
        /*
        client.setBirthDate(command.birthDate());
        client.setCivilStatus(command.civilStatus());
        client.setDistrict(command.district());
        client.setDependents(command.dependents());
        client.setCurrency(command.currency());
        client.setJobType(command.jobType());
        client.setJobMonths(command.jobMonths());
        client.setIncomeProof(command.incomeProof());
        client.setMonthlyIncome(command.monthlyIncome());
        client.setSpouseIncome(command.spouseIncome() != null ? command.spouseIncome() : 0.0);
        client.setTotalFamilyIncome(command.monthlyIncome() + (command.spouseIncome() != null ? command.spouseIncome() : 0.0));
        client.setFirstHome(command.firstHome());
        client.setHasOtherProperty(command.hasOtherProperty());
        client.setReceivedBonoBefore(command.receivedBonoBefore());
        client.setDesiredSubsidy(command.desiredSubsidy());
        */
        return clientRepository.save(client);
    }
}