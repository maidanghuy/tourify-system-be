package com.example.tourify_system_be.specification;

import com.example.tourify_system_be.dto.request.TourSearchRequest;
import com.example.tourify_system_be.entity.Tour;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TourSpecification {

    public static Specification<Tour> searchByCriteria(TourSearchRequest req) {
        return (Root<Tour> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            // ✅ Bổ sung fetch để tránh LazyInitializationException
            root.fetch("manageBy", JoinType.LEFT);
            root.fetch("place", JoinType.LEFT);
            root.fetch("category", JoinType.LEFT);

            // Tránh duplicate nếu count query
            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();

            if (req.getPlaceName() != null && !req.getPlaceName().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("place").get("placeName")),
                        "%" + req.getPlaceName().toLowerCase() + "%"
                ));
            }

            if (req.getCategoryName() != null && !req.getCategoryName().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("category").get("categoryName")),
                        "%" + req.getCategoryName().toLowerCase() + "%"
                ));
            }

            if (req.getDuration() != null) {
                predicates.add(cb.equal(root.get("duration"), req.getDuration()));
            }

            if (req.getTouristNumberAssigned() != null) {
                predicates.add(cb.equal(root.get("touristNumberAssigned"), req.getTouristNumberAssigned()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
