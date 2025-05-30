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
public class ToursImageId implements Serializable {
    static final long serialVersionUID = -4923733638919118100L;
    @Column(name = "image_id", nullable = false)
    String imageId;

    @Column(name = "tour_id", nullable = false)
    String tourId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ToursImageId entity = (ToursImageId) o;
        return Objects.equals(this.imageId, entity.imageId) &&
                Objects.equals(this.tourId, entity.tourId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageId, tourId);
    }

}