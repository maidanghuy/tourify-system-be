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
public class TourActivityId implements Serializable {
    static final long serialVersionUID = 3551556863068133668L;
    @Column(name = "tour_id", nullable = false)
    String tourId;

    @Column(name = "activity_id", nullable = false)
    String activityId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        TourActivityId entity = (TourActivityId) o;
        return Objects.equals(this.activityId, entity.activityId) &&
                Objects.equals(this.tourId, entity.tourId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, tourId);
    }

}