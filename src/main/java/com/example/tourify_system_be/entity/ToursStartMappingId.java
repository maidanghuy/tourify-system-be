package com.example.tourify_system_be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Embeddable
public class ToursStartMappingId implements Serializable {
    static final long serialVersionUID = -4008364777377881588L;
    @Column(name = "tour_id", nullable = false)
    String tourId;

    @Column(name = "start_id", nullable = false)
    String startId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ToursStartMappingId entity = (ToursStartMappingId) o;
        return Objects.equals(this.tourId, entity.tourId) &&
                Objects.equals(this.startId, entity.startId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tourId, startId);
    }

}