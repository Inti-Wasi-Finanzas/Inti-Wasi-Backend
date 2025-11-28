package com.intiwasi.platform.simulations.interfaces.rest.transform;


import com.intiwasi.platform.simulations.domain.model.commands.CreateSimulationCommand;
import com.intiwasi.platform.simulations.interfaces.rest.resources.CreateSimulationResource;

public class CreateSimulationCommandFromResourceAssembler {
    public static CreateSimulationCommand toCommand(CreateSimulationResource resource) {
        return new CreateSimulationCommand(
                resource.clientId(),
                resource.advisorId(),
                resource.programName(),
                resource.currency(),
                resource.fullName(),
                resource.dni(),
                resource.birthDate(),
                resource.email(),
                resource.phoneNumber(),
                resource.address(),
                resource.civilStatus(),
                resource.dependents(),
                resource.jobType(),
                resource.jobMonths(),
                resource.incomeProof(),
                resource.monthlyIncome(),
                resource.spouseIncomes(),
                resource.hasCurrentDebt(),
                resource.totalMonthlyDebtPayments(),
                resource.negativeRecordSbs(),
                resource.hasOtherProperty(),
                resource.receivedBonoBeforeFMV(),
                resource.typeBond(),
                resource.propertyName(),
                resource.propertyLocation(),
                resource.propertyDepartment(),
                resource.propertyDistrict(),
                resource.propertyType(),
                resource.propertyPrice(),
                resource.isPropertySustainable(),
                resource.hasDownPayment(),
                resource.percentageDownPayment(),
                resource.financialInstitution(),
                resource.deadlinesMonths(),
                resource.typeRate(),
                resource.interestRate(),
                resource.capitalization(),
                resource.monthlyCommissions(),
                resource.mortgageInsuranceRate(),
                resource.propertyInsurance(),
                resource.gracePeriodType(),
                resource.gracePeriodMonths(),
                resource.dayOfPayment()
        );
    }
}