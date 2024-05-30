package de.cleanitworks.expectum.hibernate4.domaintest;

import de.cleanitworks.expectum.core.Java8Util;
import de.cleanitworks.expectum.hibernate4.Hibernate5JsonResourceTest;
import de.cleanitworks.expectum.hibernate4.domain.Bed;
import de.cleanitworks.expectum.hibernate4.domain.Garden;
import de.cleanitworks.expectum.hibernate4.domain.Plant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GardenTest extends Hibernate5JsonResourceTest {

    long smallGardenId;

    @BeforeEach
    void beforeEachStoreAndForgetGarden() {
        Garden garden = smallGarden().build();

        doInTxn(session -> session.persist(garden));

        smallGardenId = garden.getId();
    }

    @Test
    void unresolvedCollectionSerializedToNull() {
        Garden garden = session.find(Garden.class, smallGardenId);

        jsonShow(Garden.class, "beds");
        assertThat(toJson(garden))
                .isEqualTo("{\"beds\":null}");
    }

    @Test
    void hideAllFields() {
        Garden garden = session.find(Garden.class, smallGardenId);

        jsonHide(Garden.class, "id", "beds");
        assertThat(toJson(garden))
                .isEqualTo("{}");
    }

    @Test
    void plantResolved() {
        jsonShow(Plant.class, "name");

        // given
        Garden garden = session.find(Garden.class, smallGardenId);
        Plant plant = garden.getBeds().get(0).getPlant();
        plant.getName();

        // when - then
        assertThat(toJson(plant))
                .isEqualTo("{\"name\":\"Potato\"}");
    }

    /**
     * The feature Hibernate6Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS
     * allows to show the object id of unresolved references.
     *
     * This might be a nice feature for tests having stable ids.
     * In case of instable ids (like this one, using an in-memory db) it might be good idea
     * to exclude such unresolved fields (see: jsonHide or jsonShow).
     */
    @Test
    void resolvedCollectionWithUnresolvedItems_showsItemIds() {
        jsonHide(Garden.class, "id");
        jsonShow(Bed.class, "plant");

        Garden garden = session.find(Garden.class, smallGardenId);
        // resolve collection via navigation. Items are still unresolved.
        assertThat(garden.getBeds()).hasSize(1);

        assertThat(toJson(garden))
                .startsWith("{\"beds\":[{\"plant\":{\"id\":");
    }

    @Test
    void plantUnresolved_serializesToId_evenIfHideIdWasExpressed() {
        Garden garden = session.find(Garden.class, smallGardenId);
        Plant plant = garden.getBeds().get(0).getPlant();

        jsonHide(Plant.class, "id");
        assertThat(toJson(plant))
                .startsWith("{\"id\":");
    }

    @Test
    void resolvedCollectionAndReference() {
        jsonHide(Garden.class, "id");
        jsonHide(Bed.class, "id");
        jsonHide(Plant.class, "id");

        Garden garden = session.find(Garden.class, smallGardenId);
        // trigger resolution via navigation:
        garden.getBeds().get(0).getPlant().getName();

        assertJsonNode(garden, "gardenWithResolvedBedsAndPlants");
    }

    @Test
    void resolvedCollectionAndReference_serializeChild() {
        jsonHide(Bed.class, "id");
        jsonHide(Plant.class, "id");

        Garden garden = session.find(Garden.class, smallGardenId);
        // trigger resolution via navigation:
        Bed bed = garden.getBeds().get(0);
        bed.getPlant().getName();

        assertJsonNode(bed, "bed");
    }

    Garden.GardenBuilder smallGarden() {
        return Garden.builder()
               // .id(6l)
                .beds(Java8Util.listOf(
                        Bed.builder()
                                .name("Small bed")
                                .numberOfPlants(10)
                                .plant(Plant.builder().name("Potato").build())
                                .build()));
    }

}