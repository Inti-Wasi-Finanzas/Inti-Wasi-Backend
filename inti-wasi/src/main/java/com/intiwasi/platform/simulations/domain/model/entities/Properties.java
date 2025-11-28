package com.intiwasi.platform.simulations.domain.model.entities;

import com.intiwasi.platform.simulations.domain.model.valueobjects.PropertyType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
public class Properties {

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
    private BigDecimal propertyPrice;

    @Column(name = "property_sustainable")
    private Boolean isPropertySustainable = Boolean.FALSE;
}
