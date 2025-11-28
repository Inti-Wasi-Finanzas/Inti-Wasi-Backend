package com.intiwasi.platform.clients.infrastructure.persistence.jpa.repositories;

import com.intiwasi.platform.clients.domain.model.aggregates.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    boolean existsByDni_Value(String dni);
}