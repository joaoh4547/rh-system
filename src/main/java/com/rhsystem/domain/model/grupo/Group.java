package com.rhsystem.domain.model.grupo;

import com.rhsystem.domain.model.Functionality;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;

@Entity
@Table(name = "rh_group")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "active")
    private boolean active = true;

    @Column(name = "admin")
    private boolean admin = false;

    @ElementCollection
    @CollectionTable(name = "rh_group_functionality", joinColumns = @JoinColumn(name = "group_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "functionality")
    private Collection<Functionality> functionalities;
}
