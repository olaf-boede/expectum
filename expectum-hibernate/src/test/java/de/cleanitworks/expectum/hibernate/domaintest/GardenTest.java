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
    void plantResolved() {
        var garden = session.find(Garden.class, smallGardenId);
        var plant = garden.getBeds().get(0).getPlant();
        plant.getName();

        jsonShow(Plant.class, "name");
        assertThat(toJson(plant))
                .isEqualTo("{\"name\":\"Potato\"}");
    }

    // TODO: No stable test solution for Hibernate-Jackson module behavior available yet.
    //   But it might not be critical, since content of unresolved entities might not be critical
    //   for most result verifications.
    @Test
    void plantUnresolved_serializesToId_evenIfHideIdWasExpressed() {
        var garden = session.find(Garden.class, smallGardenId);
        var plant = garden.getBeds().get(0).getPlant();

        jsonHide(Plant.class, "id");
        assertThat(toJson(plant))
                .startsWith("{\"id\":");
    }

    @Test
    void resolvedCollectionWithUnresolvedItems_showsItemIds() {
        var garden = session.find(Garden.class, smallGardenId);
        // resolve collection via navigation. Items are still unresolved.
        assertThat(garden.getBeds()).hasSize(1);

        jsonHide(Garden.class, "id");
        jsonShow(Bed.class, "plant");
        jsonHide(Plant.class, "id");

        assertThat(toJson(garden))
                .startsWith("{\"beds\":[{\"plant\":{\"id\":");
    }

    @Test
    void resolvedCollectionAndReference() {
        jsonHide(Garden.class, "id");
        jsonHide(Bed.class, "id");
        jsonHide(Plant.class, "id");

        var garden = session.find(Garden.class, smallGardenId);
        // trigger resolution via navigation:
        garden.getBeds().get(0).getPlant().getName();

        assertThat(toJson(garden)).isEqualTo(json("gardenWithResolvedBedsAndPlants"));
    }

    @Test
    void resolvedCollectionAndReference_serializeChild() {
        jsonHide(Bed.class, "id");
        jsonHide(Plant.class, "id");

        var garden = session.find(Garden.class, smallGardenId);
        // trigger resolution via navigation:
        var bed = garden.getBeds().get(0);
        bed.getPlant().getName();

        assertThat(toJson(bed)).isEqualTo(json("bed"));
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