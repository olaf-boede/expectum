package de.cleanitworks.expectum.core.resource;

import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JsonResourceExampleTest extends JsonResourceTest {

    @Test
    void simpleMeadow() {
        assertThat(toJson(Meadow.simple())).isEqualTo(json("expected"));
    }

    @Test
    void simpleMeadow_hideField() {
        jsonHide(Meadow.class, "created", "plants");

        assertThat(toJson(Meadow.simple())).isEqualTo(json("expected"));
    }
}

@Value
@Builder
class Meadow {
    String name;
    LocalDate created;
    List<Plant> plants;

    static Meadow simple() {
        return Meadow.builder()
                .name("Gulf meadow")
                .created(LocalDate.parse("2020-07-01"))
                .plants(List.of(
                        Plant.builder().name("Grass").build()
                ))
                .build();
    }
}

@Value @Builder
class Plant {
    String name;
}