package com.example.tourify_system_be.service;

import com.example.tourify_system_be.dto.request.TourSearchRequest;
import com.example.tourify_system_be.dto.response.TourResponse;
import com.example.tourify_system_be.entity.Tour;
import com.example.tourify_system_be.repository.ITourRepository;
import com.example.tourify_system_be.specification.TourSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TourService {
    private final ITourRepository itourRepository;

    public List<TourResponse> searchTours(TourSearchRequest request) {
        List<Tour> tours = itourRepository.findAll(TourSpecification.searchByCriteria(request));

        return tours.stream().map(tour -> TourResponse.builder()
                .tourId(tour.getTourId())
                .tourName(tour.getTourName())
                .description(tour.getDescription())
                .duration(tour.getDuration())
                .price(tour.getPrice())
                .minPeople(tour.getMinPeople())
                .maxPeople(tour.getMaxPeople())
                .touristNumberAssigned(tour.getTouristNumberAssigned())
                .thumbnail(tour.getThumbnail())
                .status(tour.getStatus())
                .placeName(tour.getPlace() != null ? tour.getPlace().getPlaceName() : null)
                .categoryName(tour.getCategory() != null ? tour.getCategory().getCategoryName() : null)
                .build()
        ).toList();
    }

}
