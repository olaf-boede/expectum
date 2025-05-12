package de.cleanitworks.expectum.core.resource.example.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;

@Value
@Builder
public class Meadow {
    String name;
    String description;
    LocalDate created;
    List<Plant> plants;
}