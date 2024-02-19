package de.cleanitworks.expectum.core.resource.example;

import de.cleanitworks.expectum.core.resource.JsonResourceTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MeadowHjsonTest extends JsonResourceTest {

    @Test
    void simpleMeadow() {
        assertThat(toJson(Meadow.gulf())).isEqualTo(hjson("expected"));
    }

    @Test
    void simpleMeadow_hideDescriptionAndPlants() {
        jsonHide(Meadow.class, "description", "plants");

        assertThat(toJson(Meadow.gulf())).isEqualTo(hjson("expected"));
    }

    @Test
    void simpleMeadow_showOnlyPlants() {
        jsonShow(Meadow.class, "plants");

        assertThat(toJson(Meadow.gulf())).isEqualTo(hjson("expected"));
    }
}