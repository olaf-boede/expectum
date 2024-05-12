package de.cleanitworks.expectum.hibernate4.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import static lombok.AccessLevel.PACKAGE;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = PACKAGE)
@Jacksonized
public class Plant {
    @Id
    @GeneratedValue
    Long id;

    String name;
}