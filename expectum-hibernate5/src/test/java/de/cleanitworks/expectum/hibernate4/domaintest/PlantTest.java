package de.cleanitworks.expectum.hibernate4.domaintest;

import de.cleanitworks.expectum.hibernate4.Hibernate5JsonResourceTest;
import de.cleanitworks.expectum.hibernate4.domain.Plant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlantTest extends Hibernate5JsonResourceTest {

    @Test
    void plantPersistence() {
            var plant = Plant.builder().name("Gänseblümchen").build();

            doInTxn(session -> session.persist(plant));

            var foundPlant = session.find(Plant.class, plant.getId());

            assertThat(foundPlant)
                    .isNotNull()
                    .extracting(Plant::getName)
                    .isEqualTo("Gänseblümchen");
    }
}