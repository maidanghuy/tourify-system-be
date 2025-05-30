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
public class TourPromotionId implements Serializable {
    static final long serialVersionUID = 260377405627277376L;
    @Column(name = "tour_id", nullable = false)
    String tourId;

    @Column(name = "promotion_id", nullable = false)
    String promotionId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        TourPromotionId entity = (TourPromotionId) o;
        return Objects.equals(this.tourId, entity.tourId) &&
                Objects.equals(this.promotionId, entity.promotionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tourId, promotionId);
    }

}