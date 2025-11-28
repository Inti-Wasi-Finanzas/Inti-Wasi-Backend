package com.intiwasi.platform.simulations.domain.exceptions;

public class AdvisorNotFoundException extends RuntimeException {
    //public AdvisorNotFoundException(String message) {
     //   super(message);
    //}
    public AdvisorNotFoundException(Long advisorId) {
        super(String.format("Advisor with id %s not found.", advisorId));
    }
}
