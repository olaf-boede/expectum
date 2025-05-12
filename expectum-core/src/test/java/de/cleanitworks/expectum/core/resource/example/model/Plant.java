package de.cleanitworks.expectum.core.resource.example.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Plant {
    String latinName;
    String name;
}