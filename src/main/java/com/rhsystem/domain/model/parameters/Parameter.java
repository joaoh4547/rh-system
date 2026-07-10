package com.rhsystem.domain.model.parameters;

import com.rhsystem.domain.model.shared.HasDeletion;
import com.rhsystem.domain.model.shared.HasEnable;
import com.rhsystem.infrastructure.config.CacheConfig;
import com.rhsystem.utils.CacheEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "rh_parameter")
@CacheEntity(cacheName = CacheConfig.PARAMETERS)
public class Parameter implements Serializable, HasEnable, HasDeletion {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "parameter_code")
    @Enumerated(EnumType.STRING)
    private AppParameter parameter;

    @Column(name = "parameter_name")
    private String name;

    @Column(name = "parameter_validator")
    private Class<? extends ParameterValidator> validator;

    @Column(name = "parameter_type")
    private ParameterType type;

    @Column(name = "parameter_value")
    private String value;

    @Column(name = "active")
    private boolean enable;

    @Column(name = "deleted")
    private boolean deleted;

}
