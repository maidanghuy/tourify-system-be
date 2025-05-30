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
public class TourServiceId implements Serializable {
    static final long serialVersionUID = -8697519591096579295L;
    @Column(name = "tour_id", nullable = false)
    String tourId;

    @Column(name = "service_id", nullable = false)
    String serviceId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        TourServiceId entity = (TourServiceId) o;
        return Objects.equals(this.tourId, entity.tourId) &&
                Objects.equals(this.serviceId, entity.serviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tourId, serviceId);
    }

}