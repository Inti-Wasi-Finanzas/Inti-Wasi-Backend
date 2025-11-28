package com.intiwasi.platform.simulations.domain.model.aggregates;

import com.intiwasi.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.intiwasi.platform.simulations.domain.model.valueobjects.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Table(name = "simulations")
@Getter
@Setter
public class Simulation extends AuditableAbstractAggregateRoot<Simulation> {

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "advisor_id")
    private Long advisorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "program_name", nullable = false, length = 40)
    private ProgramaHabitacional programName;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 10)
    private Currency currency;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "dni", nullable = false, length = 8)
    private String dni;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "email", nullable = false, length = 120)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    @Column(name = "address", length = 200)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "civil_status", nullable = false, length = 20)
    private CivilStatus civilStatus;

    @Column(name = "dependents")
    private Integer dependents;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 20)
    private JobType jobType;

    @Column(name = "job_months")
    private Integer jobMonths;

    @Enumerated(EnumType.STRING)
    @Column(name = "income_proof", nullable = false, length = 30)
    private IncomeProof incomeProof;

    @Column(name = "monthly_income", precision = 15, scale = 2)
    private BigDecimal monthlyIncome = BigDecimal.ZERO;

    @Column(name = "spouse_incomes", precision = 15, scale = 2)
    private BigDecimal spouseIncomes = BigDecimal.ZERO;

    @Column(name = "total_family_income", precision = 15, scale = 2)
    private BigDecimal totalFamilyIncome = BigDecimal.ZERO;

    @Column(name = "has_current_debt")
    private Boolean hasCurrentDebt = Boolean.FALSE;

    @Column(name = "total_monthly_debt_payments", precision = 15, scale = 2)
    private BigDecimal totalMonthlyDebtPayments = BigDecimal.ZERO;

    @Column(name = "negative_record_sbs")
    private Boolean negativeRecordSbs = Boolean.FALSE;


    @Column(name = "has_other_property")
    private Boolean hasOtherProperty = Boolean.FALSE;

    @Column(name = "received_bono_before_fmv")
    private Boolean receivedBonoBeforeFMV = Boolean.FALSE;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_bond", length = 20)
    private TypeBond typeBond;

    @Column(name = "property_name", length = 150)
    private String propertyName;

    @Column(name = "property_location", length = 200)
    private String propertyLocation;

    @Column(name = "property_department", length = 60)
    private String propertyDepartment;

    @Column(name = "property_district", length = 60)
    private String propertyDistrict;

    @Enumerated(EnumType.STRING)
    @Column(name = "property_type", length = 20)
    private PropertyType propertyType;

    @Column(name = "property_price", precision = 15, scale = 2)
    private BigDecimal propertyPrice = BigDecimal.ZERO;

    @Column(name = "property_sustainable")
    private Boolean isPropertySustainable = Boolean.FALSE;

    @Column(name = "has_down_payment")
    private Boolean hasDownPayment = Boolean.FALSE;

    @Column(name = "percentage_down_payment", precision = 5, scale = 2)
    private BigDecimal percentageDownPayment = BigDecimal.ZERO;

    @Column(name = "down_payment", precision = 15, scale = 2)
    private BigDecimal downPayment = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "financial_institution", length = 20)
    private FinancialInstitution financialInstitution;

    @Column(name = "amount_financed", precision = 15, scale = 2)
    private BigDecimal amountFinanced = BigDecimal.ZERO;

    @Column(name = "deadlines_months")
    private Integer deadlinesMonths; // N° de meses

    @Column(name = "type_rate", length = 10)
    private String typeRate; //TEA /TNA

    @Column(name = "interest_rate", precision = 7, scale = 4)
    private BigDecimal interestRate = BigDecimal.ZERO; //%

    @Enumerated(EnumType.STRING)
    @Column(name = "capitalization", length = 15)
    private Capitalization capitalization;

    @Column(name = "monthly_commissions", precision = 15, scale = 2)
    private BigDecimal monthlyCommissions = BigDecimal.ZERO;

    // % seguro de desgravamen por periodo (ej: 0.00028 = 0.028%)
    @Column(name = "mortgage_insurance_rate", precision = 7, scale = 5)
    private BigDecimal mortgageInsuranceRate = BigDecimal.ZERO; // seguro de desgravamen

    @Column(name = "property_insurance", precision = 15, scale = 2)
    private BigDecimal propertyInsurance; // costos de seguros (seguro de riesgo, inmueble)

    @Enumerated(EnumType.STRING)
    @Column(name = "grace_period_type", length = 15)
    private GracePeriodType gracePeriodType = GracePeriodType.NINGUNO;

    @Column(name = "grace_period_months")
    private Integer gracePeriodMonths = 0;

    @NotNull
    @Column(name = "day_of_payment", nullable = false)
    private LocalDate dayOfPayment; //fecha que se entrega el financiamiento

    @Column(name = "first_due_date")
    private LocalDate firstDueDate; //fecha de vencimiento del pago

    @Column(name = "amount_bond", precision = 15, scale = 2)
    private BigDecimal amountBond = BigDecimal.ZERO; //monto del bono

    @Column(name = "monthly_fee", precision = 15, scale = 2)
    private BigDecimal monthlyFee = BigDecimal.ZERO; //cuota mensual

    @Column(name = "van", precision = 18, scale = 2)
    private BigDecimal van;

    @Column(name = "tir", precision = 7, scale = 4)
    private BigDecimal tir;

    @Column(name = "tcea", precision = 7, scale = 4)
    private BigDecimal tcea;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 15, nullable = false)
    private EstadoSimulacion estado = EstadoSimulacion.PENDIENTE;

    @Column(name = "rejection_reason", length = 300)
    private String rejectionReason;

    @Column(name = "simulation_date", nullable = false)
    private LocalDate simulationDate = LocalDate.now();

    public Simulation() { }

    public void recalculateTotalFamilyIncome() {
        BigDecimal client = monthlyIncome != null ? monthlyIncome : BigDecimal.ZERO;
        BigDecimal spouse = spouseIncomes != null ? spouseIncomes : BigDecimal.ZERO;
        this.totalFamilyIncome = client.add(spouse);
    }

    public void recalculateDownPayment() {
        if (propertyPrice == null || percentageDownPayment == null) {
            this.downPayment = BigDecimal.ZERO;
            return;
        }
        BigDecimal pct = percentageDownPayment
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        this.downPayment = propertyPrice.multiply(pct)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void recalculateAmountFinanced() {
        BigDecimal price = propertyPrice != null ? propertyPrice : BigDecimal.ZERO;
        BigDecimal dp = downPayment != null ? downPayment : BigDecimal.ZERO;
        BigDecimal bond = amountBond != null ? amountBond : BigDecimal.ZERO;
        this.amountFinanced = price.subtract(dp).subtract(bond)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void approve() {
        this.estado = EstadoSimulacion.APROBADA;
        this.rejectionReason = null;
    }

    public void reject(String reason) {
        this.estado = EstadoSimulacion.RECHAZADA;
        this.rejectionReason = reason;
    }

    public boolean debtExceedsLimit() {
        if (Boolean.TRUE.equals(this.hasCurrentDebt)) {
            BigDecimal limit = this.totalFamilyIncome.multiply(new BigDecimal("0.30"));
            return this.totalMonthlyDebtPayments.compareTo(limit) > 0;
        }
        return false;
    }

    public boolean violatesOwnershipRules() {
        return (programName == ProgramaHabitacional.NUEVO_CREDITO_MIVIVIENDA
                || programName == ProgramaHabitacional.TECHO_PROPIO)
                && Boolean.TRUE.equals(this.hasOtherProperty);
    }

    public boolean hasHardRejectionConditions() {
        boolean notAdult = birthDate != null &&
                birthDate.plusYears(18).isAfter(LocalDate.now());
        boolean negativeSbs = Boolean.TRUE.equals(this.negativeRecordSbs);
        boolean ownershipViolation = violatesOwnershipRules();
        boolean receivedPreviousBono =
                Boolean.TRUE.equals(this.receivedBonoBeforeFMV);

        return notAdult || negativeSbs || ownershipViolation || receivedPreviousBono;
    }

    public BigDecimal toSoles(BigDecimal value) {
        if (value == null) return BigDecimal.ZERO;
        if (currency == Currency.DOLARES) {
            return value.multiply(new BigDecimal("3.386"));
        }
        return value;
    }

    public BigDecimal toCurrency(BigDecimal solesValue) {
        if (solesValue == null) return BigDecimal.ZERO;
        if (currency == Currency.DOLARES) {
            return solesValue.divide(new BigDecimal("3.386"), 6, RoundingMode.HALF_UP);
        }
        return solesValue;
    }

    public void recalculateDerivedValues() {
        recalculateTotalFamilyIncome();
        recalculateDownPayment();
        recalculateAmountFinanced();
    }
}
