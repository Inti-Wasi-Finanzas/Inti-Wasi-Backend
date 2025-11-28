package com.intiwasi.platform.simulations.interfaces.rest.transform;

import com.intiwasi.platform.simulations.domain.model.valueobjects.PaymentScheduleEntry;
import com.intiwasi.platform.simulations.interfaces.rest.resources.PaymentScheduleEntryResource;

public class PaymentScheduleEntryResourceFromValueObjectAssembler {
    public static PaymentScheduleEntryResource toResource(PaymentScheduleEntry p) {
        return new PaymentScheduleEntryResource(
                p.installmentNumber(),
                p.dueDate(),
                p.teaAnnual(),
                p.tepPeriod(),
                p.beginningBalance(),
                p.interest(),
                p.installmentWithSegDes(),
                p.amortization(),
                p.seguroDesgravamen(),
                p.seguroRiesgo(),
                p.comision(),
                p.endingBalance(),
                p.flujo()
        );
    }
}