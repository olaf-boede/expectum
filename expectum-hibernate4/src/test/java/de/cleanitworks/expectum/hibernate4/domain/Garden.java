package de.cleanitworks.expectum.hibernate4.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.List;

import static lombok.AccessLevel.PACKAGE;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = PACKAGE)
@Jacksonized
public class Garden {
    @Id
    @GeneratedValue
    Long id;

    @OneToMany(
            mappedBy = "garden",
            cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @JsonManagedReference
    List<Bed> beds;

    public static GardenBuilder builder() {
        return new GardenBuilder();
    }

    public static class GardenBuilder {
        // bi-directional mapping handling in builder.
        public Garden build() {
            var garden = new Garden(id, beds);
            garden.getBeds().forEach(b -> b.setGarden(garden));
            return garden;
        }
    }
}