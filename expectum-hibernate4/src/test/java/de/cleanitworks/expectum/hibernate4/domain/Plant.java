package de.cleanitworks.expectum.hibernate4.domain;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

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