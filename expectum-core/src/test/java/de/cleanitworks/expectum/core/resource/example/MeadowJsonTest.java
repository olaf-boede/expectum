package de.cleanitworks.expectum.core.resource.example;

import de.cleanitworks.expectum.core.resource.JsonResourceTest;
import de.cleanitworks.expectum.core.resource.example.model.Meadow;
import de.cleanitworks.expectum.core.resource.example.model.Plant;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MeadowJsonTest extends JsonResourceTest {

    final Meadow gulfMeadow = Meadow.builder()
            .name("Gulf meadow")
                .description("A green area.\nBut not helpful for nature.")
                .created(LocalDate.parse("2020-07-01"))
            .plants(List.of(
                    Plant.builder()
                            .latinName("Agrostis stolonifera INDEPENDENCE I")
                            .name("Weißes Straußgras (Zuchtform)").build(),
                    Plant.builder()
                            .latinName("Festuca rubra commutata GREENMILE")
                            .name("Rotschwingel (Zuchtform)").build()))
            .build();

    final Meadow niceMeadow = Meadow.builder()
            .name("Wildflower meadow")
            .description("A beautiful place for people and animals.")
            .created(LocalDate.parse("2020-07-01"))
            .plants(List.of(
                    Plant.builder().latinName("Festuca rubra")        .name("Horst-Rotschwingel ").build(),
                    Plant.builder().latinName("Poa angustifolia")     .name("Schmalblättriges Rispengras").build(),
                    Plant.builder().latinName("Agrimonia eupatoria")  .name("Kleiner Odermennig").build(),
                    Plant.builder().latinName("Centaurea jacea")      .name("Wiesen-Flockenblume").build()))
            .build();

    /**
     * <code>toJson(Object)</code> - converts a Java bean to a json formatted string.
     * <code>json(String)</code> - reads a string from the corresponding json node from the json file.
     */
    @Test
    void gulfMeadow_usingToJsonAndJson() {
        assertThat(toJson(gulfMeadow))
                .isEqualTo(json("expected"));
    }

    /**
     * Does the same as the test above, but:
     * <ul>
     *     <li>uses a shorter syntax and</li>
     *     <li>uses JSONassert to report differences field by field</li>
     * </ul>
     */
    @Test
    void niceMeadow_usingAssertJsonNode() {
        assertJsonNode(niceMeadow, "expected");
    }

    /**
     * This test shows how fields, which are not relevant for a test, may be excluded from the verification.
     */
    @Test
    void gulfMeadow_hideDescriptionAndPlants() {
        jsonHide(Meadow.class, "description", "plants");

        assertJsonNode(gulfMeadow, "expected");
    }

    /**
     * This test shows how only some specific fields may be included for a verification.
     */
    @Test
    void gulfMeadow_showOnlyLatinPlantNames() {
        jsonShow(Meadow.class, "plants");
        jsonShow(Plant.class, "latinName");

        assertJsonNode(gulfMeadow, "expected");
    }
}