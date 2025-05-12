package de.cleanitworks.expectum.core.resource.example;

import de.cleanitworks.expectum.core.resource.JsonResourceTest;
import de.cleanitworks.expectum.core.resource.example.model.Meadow;
import de.cleanitworks.expectum.core.resource.example.model.Plant;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static de.cleanitworks.expectum.core.Java8Util.listOf;
import static org.assertj.core.api.Assertions.assertThat;

class MeadowHjsonTest extends JsonResourceTest {

    final Meadow gulfLawn = Meadow.builder()
            .name("Gulf meadow")
            .description("A green area.\nBut not helpful for nature.")
            .created(LocalDate.parse("2020-07-01"))
            .plants(listOf(
                    Plant.builder()
                            .latinName("Agrostis stolonifera INDEPENDENCE I")
                            .name("Weißes Straußgras (Zuchtform)").build(),
                    Plant.builder()
                            .latinName("Festuca rubra commutata GREENMILE")
                            .name("Rotschwingel (Zuchtform)").build()))
            .build();

    @Test
    void gulfLawn() {
        assertThat(toJson(gulfLawn)).isEqualTo(hjson("expected"));
    }

    @Test
    void gulfLawn_hideDescriptionAndPlants() {
        jsonHide(Meadow.class, "description", "plants");

        assertThat(toJson(gulfLawn)).isEqualTo(hjson("expected"));
    }

    @Test
    void gulfLawn_showOnlyPlants() {
        jsonShow(Meadow.class, "plants");

        assertThat(toJson(gulfLawn)).isEqualTo(hjson("expected"));
    }
}