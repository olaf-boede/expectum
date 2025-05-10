package de.cleanitworks.expectum.hibernate5.domaintest;

import de.cleanitworks.expectum.hibernate5.Hibernate5JsonResourceTest;
import de.cleanitworks.expectum.hibernate5.domain.Plant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlantTest extends Hibernate5JsonResourceTest {

    @Test
    void plantPersistence() {
            Plant plant = Plant.builder().name("Gänseblümchen").build();

            doInTxn(session -> session.persist(plant));

            Plant foundPlant = session.find(Plant.class, plant.getId());

            assertThat(foundPlant)
                    .isNotNull()
                    .extracting(Plant::getName)
                    .isEqualTo("Gänseblümchen");
    }
}