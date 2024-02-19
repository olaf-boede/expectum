package de.cleanitworks.expectum.core.resource.example;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;

@Value
@Builder
class Meadow {
    String name;
    String description;
    LocalDate created;
    List<Plant> plants;

    static Meadow gulf() {
        return Meadow.builder()
                .name("Gulf meadow")
                .description("A green area.\nBut not helpful for nature.")
                .created(LocalDate.parse("2020-07-01"))
                .plants(List.of(
                        Plant.builder().name("Grass").build()
                ))
                .build();
    }
}