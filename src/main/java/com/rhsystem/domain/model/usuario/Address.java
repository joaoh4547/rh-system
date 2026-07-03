package com.rhsystem.domain.model.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Value object (DDD) representing a user's address.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Address implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "street")
    private String street;

    @Column(name = "neighborhood")
    private String neighborhood;

    @Column(name = "street_number")
    private String streetNumber;

    @Column(name = "complement")
    private String complement;

    @Column(name = "postal_code")
    private String postalCode;
}
