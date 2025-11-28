package com.intiwasi.platform.simulations.domain.exceptions;

public class ClientNotFoundException extends RuntimeException {
//    public ClientNotFoundException(String message) {
//        super(message);
//    }
    public ClientNotFoundException(Long clientId) {
        super(String.format("Client with id %s not found.", clientId));
    }
}
