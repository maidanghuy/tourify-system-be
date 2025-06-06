package com.example.tourify_system_be.specification;

import com.example.tourify_system_be.dto.request.TourSearchRequest;
import com.example.tourify_system_be.entity.Tour;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class TourSpecification {

    public static Specification<Tour> searchByCriteria(TourSearchRequest req) {
        return (Root<Tour> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Predicate predicate = cb.conjunction();

            if (req.getPlaceName() != null && !req.getPlaceName().isEmpty()) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("place").get("placeName")), "%" + req.getPlaceName().toLowerCase() + "%"));
            }

            if (req.getCategoryName() != null && !req.getCategoryName().isEmpty()) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("category").get("categoryName")), "%" + req.getCategoryName().toLowerCase() + "%"));
            }

            if (req.getDuration() != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("duration"), req.getDuration()));
            }

            if (req.getTouristNumberAssigned() != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("touristNumberAssigned"), req.getTouristNumberAssigned()));
            }

            return predicate;
        };
    }
}
