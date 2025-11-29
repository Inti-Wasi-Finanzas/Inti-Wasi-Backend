package com.intiwasi.platform.clients.domain.model.aggregates;

import com.intiwasi.platform.clients.domain.model.valueobjects.*;
import com.intiwasi.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "clients")
@Getter
@Setter
public class Client extends AuditableAbstractAggregateRoot<Client> {

    @Column(nullable = false, unique = true)
    private Long userId;

    @Embedded
    @AttributeOverride(name="value", column=@Column(name="full_name"))
    private PersonName fullName;

    @Embedded
    @AttributeOverride(name="value", column=@Column(name="dni_value"))
    private Dni dni;

    @Embedded
    @AttributeOverride(name="value", column=@Column(name="email_address"))
    private EmailAddress email;

    @Embedded
    @AttributeOverride(name="value", column=@Column(name="phone_number"))
    private PhoneNumber phone;

    private Double monthlyIncome;
    public Client() {
        this.fullName = new PersonName("Sin nombre");
        this.dni = new Dni("00000000");
        this.email = new EmailAddress("temp@intiwasi.com");
        this.phone = new PhoneNumber("999999999");
    }
}