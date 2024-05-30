package de.cleanitworks.expectum.core.resource.example;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;

import static de.cleanitworks.expectum.core.Java8Util.listOf;

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
                .plants(listOf(Plant.builder().name("Grass").build()))
                .build();
    }
}