package com.intiwasi.platform.simulations.application.internal.commandservices;


import com.intiwasi.platform.clients.infrastructure.persistence.jpa.repositories.ClientRepository;
import com.intiwasi.platform.iam.domain.model.valueobjects.Roles;
import com.intiwasi.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.intiwasi.platform.simulations.domain.model.aggregates.Simulation;
import com.intiwasi.platform.simulations.domain.model.commands.*;
import com.intiwasi.platform.simulations.domain.model.valueobjects.EstadoSimulacion;
import com.intiwasi.platform.simulations.domain.model.valueobjects.ProgramaHabitacional;
import com.intiwasi.platform.simulations.domain.services.SimulationCommandService;
import com.intiwasi.platform.simulations.domain.services.LoanCalculatorDomainService;
import com.intiwasi.platform.simulations.infrastructure.persistence.jpa.repositories.SimulationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Transactional
public class SimulationCommandServiceImpl implements SimulationCommandService {

    private final SimulationRepository simulationRepository;
    private final LoanCalculatorDomainService loanCalculatorDomainService;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    public SimulationCommandServiceImpl(SimulationRepository simulationRepository,
                                        LoanCalculatorDomainService loanCalculatorDomainService,
                                        ClientRepository clientRepository,
                                        UserRepository userRepository) {
        this.simulationRepository = simulationRepository;
        this.loanCalculatorDomainService = loanCalculatorDomainService;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Simulation handle(CreateSimulationCommand c) {

        var client = clientRepository.findById(c.clientId())
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        // 2️⃣ (Opcional) validar asesor si viene uno en el command
        if (c.advisorId() != null) {
            var advisor = userRepository.findById(c.advisorId())
                    .orElseThrow(() -> new IllegalArgumentException("Advisor not found"));
            boolean isAdvisor = advisor.getRole() == Roles.ROLE_ADVISOR;
            if (!isAdvisor) {
                throw new IllegalArgumentException("User is not an advisor");
            }
        }

        Simulation simulation = new Simulation();
        simulation.setClientId(client.getId());
        //simulation.setClientId(c.clientId());
        if (c.advisorId() != null) {
            simulation.setAdvisorId(c.advisorId());
        }

        simulation.setProgramName(c.programName());
        simulation.setCurrency(c.currency());
        simulation.setFullName(c.fullName());
        simulation.setDni(c.dni());
        simulation.setBirthDate(c.birthDate());
        simulation.setEmail(c.email());
        simulation.setPhoneNumber(c.phoneNumber());
        simulation.setAddress(c.address());
        simulation.setCivilStatus(c.civilStatus());
        simulation.setDependents(c.dependents());
        simulation.setJobType(c.jobType());
        simulation.setJobMonths(c.jobMonths());
        simulation.setIncomeProof(c.incomeProof());
        simulation.setMonthlyIncome(c.monthlyIncome());
        simulation.setSpouseIncomes(c.spouseIncomes());
        simulation.setHasCurrentDebt(c.hasCurrentDebt());
        simulation.setTotalMonthlyDebtPayments(c.totalMonthlyDebtPayments());
        simulation.setNegativeRecordSbs(c.negativeRecordSbs());
        simulation.setHasOtherProperty(c.hasOtherProperty());
        simulation.setReceivedBonoBeforeFMV(c.receivedBonoBeforeFMV());
        simulation.setTypeBond(c.typeBond());
        simulation.setPropertyName(c.propertyName());
        simulation.setPropertyLocation(c.propertyLocation());
        simulation.setPropertyDepartment(c.propertyDepartment());
        simulation.setPropertyDistrict(c.propertyDistrict());
        simulation.setPropertyType(c.propertyType());
        simulation.setPropertyPrice(c.propertyPrice());
        simulation.setIsPropertySustainable(c.isPropertySustainable());
        simulation.setHasDownPayment(c.hasDownPayment());
        simulation.setPercentageDownPayment(c.percentageDownPayment());
        simulation.setFinancialInstitution(c.financialInstitution());
        simulation.setDeadlinesMonths(c.deadlinesMonths());
        simulation.setTypeRate(c.typeRate());
        simulation.setInterestRate(c.interestRate());
        simulation.setCapitalization(c.capitalization());
        simulation.setMonthlyCommissions(c.monthlyCommissions());
        simulation.setMortgageInsuranceRate(c.mortgageInsuranceRate());
        simulation.setPropertyInsurance(c.propertyInsurance());
        simulation.setGracePeriodType(c.gracePeriodType());
        simulation.setGracePeriodMonths(c.gracePeriodMonths());
        simulation.setDayOfPayment(c.dayOfPayment());

        // 💡 Aquí usamos el "cerebro financiero"
        loanCalculatorDomainService.enrichSimulation(simulation);


        // Validaciones generales
        // 1) Mayor de edad, SBS negativo, bono previo, otra propiedad
        if (simulation.hasHardRejectionConditions()) {
            simulation.reject("Condiciones mínimas no cumplidas (Mayor de edad, SBS negativo, propiedad previa o recibió Bono FMV antes).");
            return simulationRepository.save(simulation);
        }

        // 2) Deuda mensual no debe exceder 30% del ingreso familiar
        if (simulation.debtExceedsLimit()) {
            simulation.reject("Los pagos mensuales de deuda superan el 30% del ingreso familiar.");
            return simulationRepository.save(simulation);
        }


        // Validaciones por cada programa
        BigDecimal price = simulation.getPropertyPrice();
        BigDecimal totalIncome = simulation.getTotalFamilyIncome();

        // Validaciones NUEVO CRÉDITO MI VIVIENDA
        if (simulation.getProgramName() == ProgramaHabitacional.NUEVO_CREDITO_MIVIVIENDA) {

            // MV1) Precio dentro del rango
            if (price.compareTo(new BigDecimal("68800")) < 0 ||
                    price.compareTo(new BigDecimal("362100")) > 0) {

                simulation.reject("El precio del inmueble no está dentro del rango permitido para MiVivienda.");
                return simulationRepository.save(simulation);
            }

            // MV2) Ingreso familiar > 3715
            if (totalIncome.compareTo(new BigDecimal("3715")) <= 0) {
                simulation.reject("Para MiVivienda, el ingreso familiar debe ser mayor a S/ 3,715.");
                return simulationRepository.save(simulation);
            }

            // MV3) Cuota inicial mínima = 7.5%
            if (simulation.getPercentageDownPayment().compareTo(new BigDecimal("7.5")) < 0) {
                simulation.reject("La cuota inicial mínima para MiVivienda es 7.5%.");
                return simulationRepository.save(simulation);
            }
        }


        // Validaciones TECHO PROPIO
        if (simulation.getProgramName() == ProgramaHabitacional.TECHO_PROPIO) {

            // TP1) Ingreso familiar máximo
            if (totalIncome.compareTo(new BigDecimal("3715")) > 0) {
                simulation.reject("Los ingresos familiares exceden el máximo permitido para Techo Propio (S/ 3,715).");
                return simulationRepository.save(simulation);
            }

            // TP3) Precio máximo permitido
            if (price.compareTo(new BigDecimal("130500")) > 0) {
                simulation.reject("El precio del inmueble excede el límite (S/ 130,500) para Techo Propio.");
                return simulationRepository.save(simulation);
            }

            // 1) Si el inmueble es HASTA 70,000, los ingresos NO deben superar 2,720
            if (price.compareTo(new BigDecimal("70000")) <= 0 &&
                    totalIncome.compareTo(new BigDecimal("2720")) > 0) {

                simulation.reject("Para inmuebles de hasta S/ 70,000, el ingreso familiar no debe superar S/ 2,720.");
                return simulationRepository.save(simulation);
            }

            //precio de vivienda entre más de S/70,000 y S/130,500 → ingresos entre S/2,720 y S/3,715
            if (price.compareTo(new BigDecimal("70000")) > 0 &&
                    price.compareTo(new BigDecimal("130500")) <= 0 &&
                    (totalIncome.compareTo(new BigDecimal("2720")) < 0 ||
                            totalIncome.compareTo(new BigDecimal("3715")) > 0)) {

                simulation.reject("Para inmuebles mayores a S/ 70,000 y hasta S/ 130,500, el ingreso familiar debe estar entre S/ 2,720 y S/ 3,715.");
                return simulationRepository.save(simulation);
            }


            // TP2a) VIS priorizada
            if (price.compareTo(new BigDecimal("70000")) <= 0 &&
                    totalIncome.compareTo(new BigDecimal("2720")) < 0) {

                BigDecimal dp = simulation.getPercentageDownPayment();
                if (dp.compareTo(new BigDecimal("1")) < 0 || dp.compareTo(new BigDecimal("3")) > 0) {
                    simulation.reject("Para Techo Propio VIS priorizada, la cuota inicial debe ser entre 1% y 3%.");
                    return simulationRepository.save(simulation);
                }
            }

            // TP2b) VIS regular
            if (price.compareTo(new BigDecimal("70000")) > 0 &&
                    price.compareTo(new BigDecimal("130500")) <= 0 &&
                    totalIncome.compareTo(new BigDecimal("2720")) >= 0 &&
                    totalIncome.compareTo(new BigDecimal("3715")) <= 0) {

                if (simulation.getPercentageDownPayment().compareTo(new BigDecimal("3")) < 0) {
                    simulation.reject("Para Techo Propio VIS regular, la cuota inicial mínima es 3%.");
                    return simulationRepository.save(simulation);
                }
            }
        }

        // Si pasa todas las validaciones, queda pendiente y null rechazo (ya no pongo pendiente
        // porque es el estado por defecto al crear)
        simulation.setRejectionReason(null);

        return simulationRepository.save(simulation);
    }

    @Override
    public Simulation handle(UpdateSimulationCommand c) {
        Simulation simulation = simulationRepository.findById(c.simulationId())
                .orElseThrow(() -> new IllegalArgumentException("Simulation not found"));

        simulation.setProgramName(c.programName());
        simulation.setCurrency(c.currency());
        simulation.setFullName(c.fullName());
        simulation.setDni(c.dni());
        simulation.setBirthDate(c.birthDate());
        simulation.setEmail(c.email());
        simulation.setPhoneNumber(c.phoneNumber());
        simulation.setAddress(c.address());
        simulation.setCivilStatus(c.civilStatus());
        simulation.setDependents(c.dependents());
        simulation.setJobType(c.jobType());
        simulation.setJobMonths(c.jobMonths());
        simulation.setIncomeProof(c.incomeProof());
        simulation.setMonthlyIncome(c.monthlyIncome());
        simulation.setSpouseIncomes(c.spouseIncomes());
        simulation.setHasCurrentDebt(c.hasCurrentDebt());
        simulation.setTotalMonthlyDebtPayments(c.totalMonthlyDebtPayments());
        simulation.setNegativeRecordSbs(c.negativeRecordSbs());
        simulation.setHasOtherProperty(c.hasOtherProperty());
        simulation.setReceivedBonoBeforeFMV(c.receivedBonoBeforeFMV());
        simulation.setTypeBond(c.typeBond());
        simulation.setPropertyName(c.propertyName());
        simulation.setPropertyLocation(c.propertyLocation());
        simulation.setPropertyDepartment(c.propertyDepartment());
        simulation.setPropertyDistrict(c.propertyDistrict());
        simulation.setPropertyType(c.propertyType());
        simulation.setPropertyPrice(c.propertyPrice());
        simulation.setIsPropertySustainable(c.isPropertySustainable());
        simulation.setHasDownPayment(c.hasDownPayment());
        simulation.setPercentageDownPayment(c.percentageDownPayment());
        simulation.setFinancialInstitution(c.financialInstitution());
        simulation.setDeadlinesMonths(c.deadlinesMonths());
        simulation.setTypeRate(c.typeRate());
        simulation.setInterestRate(c.interestRate());
        simulation.setCapitalization(c.capitalization());
        simulation.setMonthlyCommissions(c.monthlyCommissions());
        simulation.setMortgageInsuranceRate(c.mortgageInsuranceRate());
        simulation.setPropertyInsurance(c.propertyInsurance());
        simulation.setGracePeriodType(c.gracePeriodType());
        simulation.setGracePeriodMonths(c.gracePeriodMonths());
        simulation.setDayOfPayment(c.dayOfPayment());

        loanCalculatorDomainService.enrichSimulation(simulation);

        //Si estaba rechazada (o aprobada), al modificarla vuelve a pendiente
        if (simulation.getEstado() == EstadoSimulacion.RECHAZADA ||
                simulation.getEstado() == EstadoSimulacion.APROBADA) {

            simulation.setEstado(EstadoSimulacion.PENDIENTE);
            simulation.setRejectionReason(null);
        }

        // volver a validar todo
        if (simulation.hasHardRejectionConditions()) {
            simulation.reject("Condiciones mínimas no cumplidas (Mayor de edad, SBS negativo, propiedad previa o recibió Bono FMV antes).");
            return simulationRepository.save(simulation);
        }

        if (simulation.debtExceedsLimit()) {
            simulation.reject("Los pagos mensuales de deuda superan el 30% del ingreso familiar.");
            return simulationRepository.save(simulation);
        }

        // Validación por programa (igual que en create)
        BigDecimal price = simulation.getPropertyPrice();
        BigDecimal totalIncome = simulation.getTotalFamilyIncome();

        // Mi Vivienda
        if (simulation.getProgramName() == ProgramaHabitacional.NUEVO_CREDITO_MIVIVIENDA) {

            if (price.compareTo(new BigDecimal("68800")) < 0 ||
                    price.compareTo(new BigDecimal("362100")) > 0) {

                simulation.reject("El precio del inmueble no está dentro del rango permitido para MiVivienda.");
                return simulationRepository.save(simulation);
            }

            if (totalIncome.compareTo(new BigDecimal("3715")) <= 0) {
                simulation.reject("Para MiVivienda, el ingreso familiar debe ser mayor a S/ 3,715.");
                return simulationRepository.save(simulation);
            }

            if (simulation.getPercentageDownPayment().compareTo(new BigDecimal("7.5")) < 0) {
                simulation.reject("La cuota inicial mínima para MiVivienda es 7.5%.");
                return simulationRepository.save(simulation);
            }
        }

        // Techo Propio
        if (simulation.getProgramName() == ProgramaHabitacional.TECHO_PROPIO) {

            if (totalIncome.compareTo(new BigDecimal("3715")) > 0) {
                simulation.reject("Los ingresos familiares exceden el máximo permitido para Techo Propio (S/ 3,715).");
                return simulationRepository.save(simulation);
            }

            if (price.compareTo(new BigDecimal("130500")) > 0) {
                simulation.reject("El precio del inmueble excede el límite (S/ 130,500) para Techo Propio.");
                return simulationRepository.save(simulation);
            }

            // 1) Hasta 70,000 → ingresos <= 2,720
            if (price.compareTo(new BigDecimal("70000")) <= 0 &&
                    totalIncome.compareTo(new BigDecimal("2720")) > 0) {

                simulation.reject("Para inmuebles de hasta S/ 70,000, el ingreso familiar no debe superar S/ 2,720.");
                return simulationRepository.save(simulation);
            }

            //precio de vivienda entre más de S/70,000 y S/130,500 → ingresos entre S/2,720 y S/3,715
            if (price.compareTo(new BigDecimal("70000")) > 0 &&
                    price.compareTo(new BigDecimal("130500")) <= 0 &&
                    (totalIncome.compareTo(new BigDecimal("2720")) < 0 ||
                            totalIncome.compareTo(new BigDecimal("3715")) > 0)) {

                simulation.reject("Para inmuebles mayores a S/ 70,000 y hasta S/ 130,500, el ingreso familiar debe estar entre S/ 2,720 y S/ 3,715.");
                return simulationRepository.save(simulation);
            }

            //Vis Priorizado
            if (price.compareTo(new BigDecimal("70000")) <= 0 && // esto es entre 0 y 70000
                    totalIncome.compareTo(new BigDecimal("2720")) < 0) { // esto es entre 0 y 2720

                BigDecimal dp = simulation.getPercentageDownPayment(); //dp es downPayment
                if (dp.compareTo(new BigDecimal("1")) < 0 || dp.compareTo(new BigDecimal("3")) > 0) {
                    simulation.reject("Para Techo Propio VIS priorizada(la cuota inicial debe ser entre 1% y 3%."); //para nivel socioeconomico(NSE) D y E. D: ganar un promedio familiar S/2720
                    //E: tiene un ingreso promedio familiar de S/2052
                    return simulationRepository.save(simulation);
                }
            }

            if (price.compareTo(new BigDecimal("70000")) > 0 &&
                    price.compareTo(new BigDecimal("130500")) <= 0 &&
                    totalIncome.compareTo(new BigDecimal("2720")) >= 0 &&
                    totalIncome.compareTo(new BigDecimal("3715")) <= 0) {

                if (simulation.getPercentageDownPayment().compareTo(new BigDecimal("3")) < 0) {
                    simulation.reject("Para Techo Propio VIS regular, la cuota inicial mínima es 3%.");
                    return simulationRepository.save(simulation);
                }
            }
        }

        return simulationRepository.save(simulation);
    }


    @Override
    public void handle(DeleteSimulationCommand c) {
        simulationRepository.deleteById(c.simulationId());
    }


    @Override
    public Simulation handle(ApproveSimulationCommand c) {
        Simulation simulation = simulationRepository.findById(c.simulationId())
                .orElseThrow(() -> new IllegalArgumentException("Simulation not found"));

        var advisor = userRepository.findById(c.advisorId())
                .orElseThrow(() -> new IllegalArgumentException("Advisor not found"));
        boolean isAdvisor = advisor.getRole() == Roles.ROLE_ADVISOR;
        if (!isAdvisor) {
            throw new IllegalArgumentException("User is not an advisor");
        }

        simulation.setAdvisorId(c.advisorId());
        simulation.approve();
        return simulationRepository.save(simulation);
    }

    @Override
    public Simulation handle(RejectSimulationCommand c) {
        Simulation simulation = simulationRepository.findById(c.simulationId())
                .orElseThrow(() -> new IllegalArgumentException("Simulation not found"));

        var advisor = userRepository.findById(c.advisorId())
                .orElseThrow(() -> new IllegalArgumentException("Advisor not found"));
        boolean isAdvisor = advisor.getRole() == Roles.ROLE_ADVISOR;
        if (!isAdvisor) {
            throw new IllegalArgumentException("User is not an advisor");
        }

        simulation.setAdvisorId(c.advisorId());
        simulation.reject(c.reason());
        return simulationRepository.save(simulation);
    }
}
