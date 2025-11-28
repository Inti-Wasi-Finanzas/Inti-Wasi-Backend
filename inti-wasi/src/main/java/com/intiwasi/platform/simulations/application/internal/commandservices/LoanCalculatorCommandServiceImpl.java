package com.intiwasi.platform.simulations.application.internal.commandservices;

import com.intiwasi.platform.simulations.domain.model.aggregates.Simulation;
import com.intiwasi.platform.simulations.domain.model.valueobjects.*;
import com.intiwasi.platform.simulations.domain.services.LoanCalculatorDomainService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class LoanCalculatorCommandServiceImpl implements LoanCalculatorDomainService {

    private static final int SCALE_CALC = 10;
    private static final int SCALE_MONEY = 2;
    private static final MathContext MC = new MathContext(16, RoundingMode.HALF_UP);

    @Override
    public void enrichSimulation(Simulation simulation) {
        simulation.recalculateDerivedValues();  // ingresos, cuota inicial, monto financiado

        //bono
        BigDecimal bond = calculateBond(simulation);
        simulation.setAmountBond(bond);
        simulation.recalculateAmountFinanced();

        //cronograma
        List<PaymentScheduleEntry> schedule = generateSchedule(simulation);

        if (!schedule.isEmpty()) {
            // cuota fija total (flujo) del primer periodo con amortización
            BigDecimal firstFlowWithAmort = schedule.stream()
                    .filter(e -> e.amortization().compareTo(BigDecimal.ZERO) > 0)
                    .findFirst()
                    .map(PaymentScheduleEntry::flujo)
                    .orElse(schedule.get(schedule.size() - 1).flujo());

            simulation.setMonthlyFee(
                    firstFlowWithAmort.setScale(SCALE_MONEY, RoundingMode.HALF_UP)
            );
        }

        //Los indicadores
        FinancialIndicators indicators = calculateIndicators(simulation, schedule);
        simulation.setVan(indicators.van());
        simulation.setTir(indicators.tir());
        simulation.setTcea(indicators.tcea());
    }

    //generación del cronograma
    @Override
    public List<PaymentScheduleEntry> generateSchedule(Simulation simulation) {
        List<PaymentScheduleEntry> schedule = new ArrayList<>();

        BigDecimal principal = nvl(simulation.getAmountFinanced()).setScale(SCALE_CALC, RoundingMode.HALF_UP);
        if (principal.compareTo(BigDecimal.ZERO) <= 0) return schedule;

        int totalMonths = simulation.getDeadlinesMonths() != null ? simulation.getDeadlinesMonths() : 0;
        if (totalMonths <= 0) return schedule;

        int graceMonths = simulation.getGracePeriodMonths() != null ? simulation.getGracePeriodMonths() : 0;
        GracePeriodType graceType = simulation.getGracePeriodType() != null
                ? simulation.getGracePeriodType()
                : GracePeriodType.NINGUNO;


        //BigDecimal tea = nvl(simulation.getInterestRate())
        //        .divide(BigDecimal.valueOf(100), SCALE_CALC, RoundingMode.HALF_UP);

        //si el usuario puso TNA se convierte a TEA y luego a TEP
        BigDecimal tea = getAnnualTEA(simulation);
        BigDecimal tep = calculatePeriodicTEP(simulation);

        // Cálculo del Seguro de Desgravamen (sobre Saldo)
        BigDecimal sdpRatePercent = nvl(simulation.getMortgageInsuranceRate()); // 0.0003
        BigDecimal sdpRate = sdpRatePercent
                .divide(BigDecimal.valueOf(100), SCALE_CALC, RoundingMode.HALF_UP);
        // 0.0003 / 100 = 0.000003  => 0.0003%


        // cálculo del Seguro de Riesgo Fijo (sobre Inmueble)
        // propertyInsurance es la Tasa Anual en Porcentaje (e.g., 0.25 para 0.25%)
        BigDecimal seguroRiesgoRateRaw = nvl(simulation.getPropertyInsurance());
        BigDecimal seguroRiesgoMensual;

        if (seguroRiesgoRateRaw.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal propertyPrice = nvl(simulation.getPropertyPrice());

            // convertir la tasa anual a decimal (ej: 0.25 -> 0.0025)
            BigDecimal annualRateDecimal = seguroRiesgoRateRaw
                    .divide(BigDecimal.valueOf(100), SCALE_CALC, RoundingMode.HALF_UP);

            // monto mensual fijo: (Precio del Inmueble * Tasa Anual Decimal) / 12
            seguroRiesgoMensual = propertyPrice
                    .multiply(annualRateDecimal)
                    .divide(BigDecimal.valueOf(12), SCALE_CALC, RoundingMode.HALF_UP);

        } else {
            seguroRiesgoMensual = BigDecimal.ZERO;
        }

        BigDecimal seguroRiesgoFijo = seguroRiesgoMensual.setScale(SCALE_CALC, RoundingMode.HALF_UP);

        BigDecimal comisionFija = nvl(simulation.getMonthlyCommissions()).setScale(SCALE_CALC, RoundingMode.HALF_UP);

        LocalDate firstPaymentDate = simulation.getDayOfPayment();
        BigDecimal currentBalance = principal;

        int N = totalMonths - graceMonths;

        for (int nPeriodo = 1; nPeriodo <= totalMonths; nPeriodo++) {
            BigDecimal saldoInicial = currentBalance;

            // Interés del periodo
            BigDecimal interes = saldoInicial.multiply(tep).setScale(SCALE_CALC, RoundingMode.HALF_UP);
            // Seguro de desgravamen del periodo
            BigDecimal segDes = saldoInicial.multiply(sdpRate).setScale(SCALE_CALC, RoundingMode.HALF_UP);

            BigDecimal cuotaIncSegDes = BigDecimal.ZERO;
            BigDecimal amortizacion = BigDecimal.ZERO;
            BigDecimal saldoFinal;
            BigDecimal flujo;

            boolean enGracia = nPeriodo <= graceMonths && graceType != GracePeriodType.NINGUNO;

            if (enGracia) {
                // PERÍODO EN GRACIA
                if (graceType == GracePeriodType.TOTAL) {
                    // Cuota = 0, se capitaliza el interés (y seg. desgravamen si quieres estrictamente igual al Excel)
                    cuotaIncSegDes = BigDecimal.ZERO;
                    amortizacion = BigDecimal.ZERO;
                    saldoFinal = saldoInicial.add(interes).setScale(SCALE_CALC, RoundingMode.HALF_UP);
                    // Flujo = Cuota + SegRie + Comision + SegDes
                    flujo = cuotaIncSegDes
                            .add(seguroRiesgoFijo)
                            .add(comisionFija)
                            .add(segDes);
                } else { // PARCIAL
                    // Cuota = Interés (según la regla del pdf)
                    cuotaIncSegDes = interes;
                    amortizacion = BigDecimal.ZERO;
                    saldoFinal = saldoInicial;
                    flujo = cuotaIncSegDes
                            .add(seguroRiesgoFijo)
                            .add(comisionFija)
                            .add(segDes);
                }
            } else {
                // SIN GRACIA
                int nc = nPeriodo - graceMonths;
                BigDecimal iEff = tep.add(sdpRate);

                BigDecimal cuotaBase;

                if (iEff.compareTo(BigDecimal.ZERO) == 0) {
                    int remaining = N - nc + 1;
                    cuotaBase = saldoInicial
                            .divide(BigDecimal.valueOf(remaining), SCALE_CALC, RoundingMode.HALF_UP);
                } else {
                    int exponent = N - nc + 1;

                    BigDecimal unoMas = BigDecimal.ONE.add(iEff, MC); //esto es para evitar pérdida de precisión
                    BigDecimal pow = unoMas.pow(exponent, MC); // (1 + iEff)^(N - nc + 1)

                    BigDecimal numerador = iEff.multiply(pow, MC); // iEff * (1 + iEff)^(N - nc + 1)
                    BigDecimal denominador = pow.subtract(BigDecimal.ONE, MC); // (1 + iEff)^(N - nc + 1) - 1

                    cuotaBase = saldoInicial // saldoInicial * [iEff * (1 + iEff)^(N - nc + 1)] / [(1 + iEff)^(N - nc + 1) - 1]
                            .multiply(numerador, MC)
                            .divide(denominador, SCALE_CALC, RoundingMode.HALF_UP);
                }

                // cuotaBase = cuota (inc seg des)
                cuotaIncSegDes = cuotaBase;
                amortizacion = cuotaIncSegDes
                        .subtract(interes)
                        .subtract(segDes)
                        .setScale(SCALE_CALC, RoundingMode.HALF_UP);

                if (nPeriodo == totalMonths) {
                    amortizacion = saldoInicial;
                    cuotaIncSegDes = interes.add(segDes).add(amortizacion);
                }

                saldoFinal = saldoInicial
                        .subtract(amortizacion)
                        .setScale(SCALE_CALC, RoundingMode.HALF_UP);
                if (saldoFinal.compareTo(BigDecimal.ZERO) < 0) saldoFinal = BigDecimal.ZERO;

                // Flujo = Cuota + SeguroRiesgo + Comision (según regla)
                flujo = cuotaIncSegDes
                        .add(seguroRiesgoFijo)
                        .add(comisionFija)
                        .negate();
            }

            LocalDate dueDate = firstPaymentDate.plusMonths(nPeriodo - 1L);

            schedule.add(new PaymentScheduleEntry(
                    nPeriodo,
                    dueDate,
                    roundPercent(tea),
                    roundPercent(tep),
                    roundMoney(saldoInicial),
                    roundMoney(interes),
                    roundMoney(cuotaIncSegDes),
                    roundMoney(amortizacion),
                    roundMoney(segDes),
                    roundMoney(seguroRiesgoFijo),
                    roundMoney(comisionFija),
                    roundMoney(saldoFinal),
                    roundMoney(flujo)
            ));

            currentBalance = saldoFinal;
        }

        return schedule;
    }

    // BONOS

    private BigDecimal calculateBond(Simulation simulation) {
        ProgramaHabitacional program = simulation.getProgramName();
        BigDecimal price = nvl(simulation.getPropertyPrice());
        BigDecimal totalIncome = nvl(simulation.getTotalFamilyIncome());
        TypeBond bondType = simulation.getTypeBond();
        boolean sustainable = Boolean.TRUE.equals(simulation.getIsPropertySustainable());
        PropertyType propertyType = simulation.getPropertyType();

        if (program == ProgramaHabitacional.NUEVO_CREDITO_MIVIVIENDA) {
            BigDecimal min = new BigDecimal("68800");
            BigDecimal max = new BigDecimal("362100");
            if (price.compareTo(min) < 0 || price.compareTo(max) > 0) return BigDecimal.ZERO;

            boolean isSostenible = bondType == TypeBond.BBP_SOSTENIBLE || sustainable;
            BigDecimal bond;
            if (price.compareTo(new BigDecimal("98100")) <= 0) {
                bond = isSostenible ? new BigDecimal("33700") : new BigDecimal("27400");
            } else if (price.compareTo(new BigDecimal("146900")) <= 0) {
                bond = isSostenible ? new BigDecimal("29100") : new BigDecimal("22800");
            } else if (price.compareTo(new BigDecimal("244600")) <= 0) {
                bond = isSostenible ? new BigDecimal("27200") : new BigDecimal("20900");
            } else {
                bond = isSostenible ? new BigDecimal("14100") : new BigDecimal("7800");
            }
            return bond;
        }

        if (program == ProgramaHabitacional.TECHO_PROPIO && bondType == TypeBond.BFH) {
            BigDecimal maxIncomeToBuy = new BigDecimal("3715");
            if (totalIncome.compareTo(maxIncomeToBuy) > 0) return BigDecimal.ZERO;

            boolean isVisPriorizada = totalIncome.compareTo(new BigDecimal("1300")) < 0;
            boolean isCasa = propertyType == PropertyType.CASA;
            boolean isDepa = propertyType == PropertyType.DEPARTAMENTO;

            if (isVisPriorizada) {
                if (isCasa && price.compareTo(new BigDecimal("60000")) <= 0) {
                    return new BigDecimal("56710");
                }
                if (isDepa && price.compareTo(new BigDecimal("70000")) <= 0) {
                    return new BigDecimal("51895");
                }
                return BigDecimal.ZERO;
            }

            if (isCasa && price.compareTo(new BigDecimal("104500")) <= 0) {
                return new BigDecimal("50825");
            }
            if (isDepa && price.compareTo(new BigDecimal("136000")) <= 0) {
                return new BigDecimal("46545");
            }
            return BigDecimal.ZERO;
        }

        return BigDecimal.ZERO;
    }

    //TEA / TNA
    private BigDecimal getAnnualTEA(Simulation simulation) {
        BigDecimal ratePercent = nvl(simulation.getInterestRate());
        if (ratePercent.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        BigDecimal rate = ratePercent
                .divide(BigDecimal.valueOf(100)); // fracción

        String typeRate = simulation.getTypeRate() != null
                ? simulation.getTypeRate().trim().toUpperCase()
                : "TEA";

        Capitalization cap = simulation.getCapitalization() != null
                ? simulation.getCapitalization()
                : Capitalization.MENSUAL;

        if ("TEA".equals(typeRate)) {
            // Ya es TEA
            return rate; // TEA = fracción exacta
        }

        // Si es TNA -> convertir a TEA
        int m = switch (cap) {
            case MENSUAL -> 12;
            case TRIMESTRAL -> 4;
            case SEMESTRAL -> 2;
            case ANUAL -> 1;
            case DIARIA -> 360;
        };

        double tn = rate.doubleValue();
        double tea = Math.pow(1 + tn / m, m) - 1;
        return BigDecimal.valueOf(tea);
    }


    //  TEP

    private BigDecimal calculatePeriodicTEP(Simulation simulation) {
        BigDecimal tea = getAnnualTEA(simulation);
        if(tea.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        double teaD = tea.doubleValue();
        double tepMonthlyD = Math.pow(1 + teaD, 30.0 / 360.0) - 1;

        return new BigDecimal(tepMonthlyD);
        //return BigDecimal.valueOf(tepMonthlyD).setScale(SCALE_CALC, RoundingMode.HALF_UP);
    }

    // Aqui se halla la VAN, TIR, TCEA

    private FinancialIndicators calculateIndicators(Simulation simulation, List<PaymentScheduleEntry> schedule) {
        BigDecimal principal = nvl(simulation.getAmountFinanced());
        if (principal.compareTo(BigDecimal.ZERO) <= 0 || schedule.isEmpty()) {
            return new FinancialIndicators(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        // 1) COK anual y COK del periodo (mensual)
        BigDecimal cokAnnual = getCokAnnual(simulation);          // 13.99%, 13.10%, etc (en fracción)
        BigDecimal cokPeriod = getPeriodicFromAnnual(cokAnnual);  // Tasa mensual equivalente

        // 2) Construimos flujo “tipo inversión”:
        //    t0 = -inversión (desembolso del banco)
        //    t1..tn = +flujos que paga el cliente (cuota + seguros + comisiones)
        double[] cf = new double[schedule.size() + 1];

        // Inversión inicial (NEGATIVA)
        cf[0] = -principal.doubleValue(); //cf es cashflows

        // Los flujos en tu schedule vienen como negativos (salida para el cliente),
        // así que los negamos para tenerlos como ENTRADAS del proyecto (banco).
        for (int i = 0; i < schedule.size(); i++) {
            cf[i + 1] = schedule.get(i).flujo().negate().doubleValue();
        }

        // VAN
        double npv = 0.0;
        double cok = cokPeriod.doubleValue();
        for (int t = 0; t < cf.length; t++) {
            npv += cf[t] / Math.pow(1 + cok, t);
        }
    // Para que la VAN tenga el mismo signo que el Excel:
        BigDecimal van = BigDecimal.valueOf(-npv)       //signo invertido (para que sea perspectiva del banco)
                .setScale(SCALE_MONEY, RoundingMode.HALF_UP);

    // IRR mensual
        double irrMonthly = irr(cf);

    // TIR mensual (%)
        BigDecimal tir = BigDecimal.valueOf(irrMonthly * 100)
                .setScale(5, RoundingMode.HALF_UP);

    // TCEA anual (%), usando frecuencia mensual: 360/30 = 12
        double tceaAnnual = Math.pow(1 + irrMonthly, 12) - 1;
        BigDecimal tcea = BigDecimal.valueOf(tceaAnnual * 100)
                .setScale(5, RoundingMode.HALF_UP);

        return new FinancialIndicators(van, tir, tcea);
    }


    private BigDecimal getCokAnnual(Simulation sim) {

        boolean isSoles = sim.getCurrency() == Currency.SOLES;
        BigDecimal price = nvl(sim.getPropertyPrice());

        switch (sim.getFinancialInstitution()) {

            case BCP -> {
                return isSoles
                        ? new BigDecimal("0.1399")
                        : new BigDecimal("0.1270");
            }

            case BBVA -> {
                if (sim.getProgramName() == ProgramaHabitacional.NUEVO_CREDITO_MIVIVIENDA) {
                    if (price.compareTo(new BigDecimal("95000")) < 0)
                        return new BigDecimal("0.1310");
                    else
                        return new BigDecimal("0.1290");
                } else {
                    return new BigDecimal("0.20");
                }
            }

            case INTERBANK -> {
                if (sim.getProgramName() == ProgramaHabitacional.NUEVO_CREDITO_MIVIVIENDA) {

                    if (price.compareTo(new BigDecimal("100000")) <= 0)
                        return new BigDecimal("0.1260");
                    if (price.compareTo(new BigDecimal("200000")) <= 0)
                        return new BigDecimal("0.1230");
                    if (price.compareTo(new BigDecimal("300000")) <= 0)
                        return new BigDecimal("0.1220");

                    return new BigDecimal("0.1190");
                }
                else {
                    return new BigDecimal("0.16");
                }
            }
        }

        return BigDecimal.ZERO;
    }

    private BigDecimal getPeriodicFromAnnual(BigDecimal annual) {
        if (annual.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        double teaD = annual.doubleValue();
        double tepD = Math.pow(1 + teaD, 30.0 / 360.0) - 1;
        return BigDecimal.valueOf(tepD).setScale(SCALE_CALC, RoundingMode.HALF_UP);
    }

    // IRR usando método de bisección
    private double irr(double[] cashflows) {
        double low = -0.9999; // -99.99%
        double high = 10.0; // 1000%
        double mid = 0.0; // valor intermedio

        for (int iter = 0; iter < 100; iter++) { // máximo 100 iteraciones
            mid = (low + high) / 2.0; // punto medio
            double npv = 0.0; // valor presente neto. NPV significa Valor Presente Neto
            for (int t = 0; t < cashflows.length; t++) { // calcular NPV
                npv += cashflows[t] / Math.pow(1 + mid, t); // fórmula NPV
            }
            if (Math.abs(npv) < 1e-8) break; // si NPV es cercano a 0, salir
            if (npv > 0) low = mid; else high = mid; // ajustar rango
        }
        return mid; // TIR aproximada
    }

    
    //  Helpers
    
    // Evita nulls en BigDecimal
    private BigDecimal nvl(BigDecimal v) { //esto es para evitar nulls
        return v != null ? v : BigDecimal.ZERO;
    }

    // Redondeos en montos
    private BigDecimal roundMoney(BigDecimal v) { //esto es para mostrar en dinero
        if (v == null) return BigDecimal.ZERO.setScale(SCALE_MONEY, RoundingMode.HALF_UP);
        return v.setScale(SCALE_MONEY, RoundingMode.HALF_UP);
    }

    // Redondeos en %
    private BigDecimal roundPercent(BigDecimal v) { //esto es para mostrar en %
        if (v == null) return BigDecimal.ZERO;
        return v;
    }

    // Helper interno para indicadores financieros
    private record FinancialIndicators( //esto es solo un helper interno
        BigDecimal van,
        BigDecimal tir,
        BigDecimal tcea
    ) { }
}