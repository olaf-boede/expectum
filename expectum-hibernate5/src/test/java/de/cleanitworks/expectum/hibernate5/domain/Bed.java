package de.cleanitworks.expectum.hibernate5.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import javax.persistence.*;

import static lombok.AccessLevel.PACKAGE;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = PACKAGE)
@Jacksonized
public class Bed {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    String name;

    @ManyToOne(fetch = FetchType.LAZY)
    // Prevent json serialization cycle for the parent reference.
    @JsonBackReference
    Garden garden;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    Plant plant;

    int numberOfPlants;
}