package de.cleanitworks.expectum.hibernate.domaintest;

import de.cleanitworks.expectum.hibernate.HibernateJsonResourceTest;
import de.cleanitworks.expectum.hibernate.domain.Bed;
import de.cleanitworks.expectum.hibernate.domain.Garden;
import de.cleanitworks.expectum.hibernate.domain.Plant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GardenTest extends HibernateJsonResourceTest {

    long smallGardenId;

    @BeforeEach
    void beforeEachStoreAndForgetGarden() {
        var garden = smallGarden().build();

        doInTxn(session -> {
            session.persist(garden);
        });

        smallGardenId = garden.getId();
    }

    @Test
    void unresolvedCollectionSerializedToNull() {
        var garden = session.find(Garden.class, smallGardenId);

        jsonShow(Garden.class, "beds");
        assertThat(toJson(garden))
                .isEqualTo("{\"beds\":null}");
    }

    @Test
    void hideAllFields() {
        var garden = session.find(Garden.class, smallGardenId);

        jsonHide(Garden.class, "id", "beds");
        assertThat(toJson(garden))
                .isEqualTo("{}");
    }

    @Test
    void plantRelationUnresolvedAndResolved() {
        jsonShow(Plant.class, "name");

        // given
        var garden = session.find(Garden.class, smallGardenId);
        var plant = garden.getBeds().get(0).getPlant();

        // when unresolved proxy - only the key will be shown
        assertThat(toJson(plant))
                // .matches("{\"id\":*}") // XXX: does not compile. Why?
                .startsWith("{\"id\":");

        // when relation is resolved - the bean field values will be reported.
        plant.getName();
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

        var garden = session.find(Garden.class, smallGardenId);
        // resolve collection via navigation. Items are still unresolved.
        assertThat(garden.getBeds()).hasSize(1);

        assertThat(toJson(garden))
                .startsWith("{\"beds\":[{\"plant\":{\"id\":");
    }

    @Test
    void plantUnresolved_serializesToId_evenIfHideIdWasExpressed() {
        var garden = session.find(Garden.class, smallGardenId);
        var plant = garden.getBeds().get(0).getPlant();

        jsonHide(Plant.class, "id");
        assertThat(toJson(plant))
                .startsWith("{\"id\":");
    }

    @Test
    void resolvedCollectionAndReference() {
        jsonHide(Garden.class, "id");
        jsonHide(Bed.class, "id");
        jsonHide(Plant.class, "id");

        var garden = session.find(Garden.class, smallGardenId);
        // trigger resolution via navigation:
        garden.getBeds().get(0).getPlant().getName();

        assertJsonNode(garden, "gardenWithResolvedBedsAndPlants");
    }

    @Test
    void resolvedCollectionAndReference_serializeChild() {
        jsonHide(Bed.class, "id");
        jsonHide(Plant.class, "id");

        var garden = session.find(Garden.class, smallGardenId);
        // trigger resolution via navigation:
        var bed = garden.getBeds().get(0);
        bed.getPlant().getName();

        assertJsonNode(bed, "bed");
    }

    Garden.GardenBuilder smallGarden() {
        return Garden.builder()
               // .id(6l)
                .beds(List.of(
                        Bed.builder()
                                .name("Small bed")
                                .numberOfPlants(10)
                                .plant(Plant.builder().name("Potato").build())
                                .build()));
    }

}