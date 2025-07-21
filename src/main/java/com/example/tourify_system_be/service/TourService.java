package com.example.tourify_system_be.service;

import com.example.tourify_system_be.dto.request.TourCreateRequest;
import com.example.tourify_system_be.dto.request.TourFilterRequest;
import com.example.tourify_system_be.dto.request.TourSearchRequest;
import com.example.tourify_system_be.dto.request.TourUpdateRequest;
import com.example.tourify_system_be.dto.response.TourResponse;
import com.example.tourify_system_be.entity.*;
import com.example.tourify_system_be.exception.AppException;
import com.example.tourify_system_be.exception.ErrorCode;
import com.example.tourify_system_be.mapper.TourMapper;
import com.example.tourify_system_be.repository.*;
import com.example.tourify_system_be.specification.TourSpecification;
import com.example.tourify_system_be.repository.IToursStartMappingRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.tourify_system_be.security.JwtUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TourService {

    private final ITourRepository itourRepository;
    private final IFeedbackRepository IFeedbackRepository;
    private final IBookingTourRepository IBookingRepository;
    private final TourMapper tourMapper;
    private final IUserRepository iUserRepository;
    private final IPlaceRepository iPlaceRepository;
    private final ICategoryRepository iCategoryRepository;
    private final ITourRepository tourRepository;
    private final IFeedbackRepository iFeedbackRepository;
    private final IToursStartMappingRepository iToursStartMappingRepository;
    private final IToursStartRepository toursStartRepository;
    private final IToursStartMappingRepository toursStartMappingRepository;
    private final ITourActivityRepository iTourActivityRepository;
    private final ITourServicesRepository iTourServicesRepository;
    private final IActivityRepository iActivityRepository;
    private final IServicesRepository iServicesRepository;
    private final JwtUtil jwtUtil;

    public List<TourResponse> searchTours(TourSearchRequest request) {
        List<Tour> tours = itourRepository.findAll(TourSpecification.searchByCriteria(request));

        return tours.stream().map(tour -> {
            // Map từ entity sang DTO
            TourResponse res = tourMapper.toResponse(tour);

            // Tính và gán rating trung bình từ feedback
            BigDecimal avgRating = IFeedbackRepository.findAverageRatingByTourId(tour.getTourId());
            res.setRating(avgRating != null ? avgRating.setScale(1, RoundingMode.HALF_UP) : null);

            return res;
        }).toList();
    }

    public List<TourResponse> filterTours(TourFilterRequest filter) {
        return filter.getBaseTours().stream()
                .filter(tour -> {
                    boolean matches = true;

                    if (filter.getMinPrice() != null)
                        matches &= tour.getPrice() != null &&
                                tour.getPrice().compareTo(filter.getMinPrice()) >= 0;

                    if (filter.getMaxPrice() != null)
                        matches &= tour.getPrice() != null &&
                                tour.getPrice().compareTo(filter.getMaxPrice()) <= 0;

                    if (filter.getMinRating() != null)
                        matches &= tour.getRating() != null &&
                                tour.getRating().compareTo(filter.getMinRating()) >= 0;

                    if (filter.getMaxRating() != null)
                        matches &= tour.getRating() != null &&
                                tour.getRating().compareTo(filter.getMaxRating()) <= 0;

                    if (filter.getCreatedByUserName() != null && !filter.getCreatedByUserName().isEmpty())
                        matches &= tour.getCreatedByUserName() != null &&
                                tour.getCreatedByUserName().toLowerCase()
                                        .contains(filter.getCreatedByUserName().toLowerCase());

                    return matches;
                })
                .toList();
    }

    public List<TourResponse> getAllToursWithDetails(String bearerToken) {
        // 1. Extract userId từ token
        String userId = null;
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            userId = jwtUtil.extractUserId(token);
        }

        // 2. Nếu không có userId => không có tour nào
        if (userId == null) {
            return Collections.emptyList();
            // hoặc: throw new UnauthorizedException("Invalid token");
        }

        // 3. Chỉ lấy tour do chính sub-company đó quản lý
        List<Tour> tours = tourRepository.findAllByManageBy_UserId(userId);

        // 4. Mapping và bổ sung các trường động
        return tours.stream().map(tour -> {
            TourResponse resp = tourMapper.toResponse(tour);

            resp.setCreatedAt(tour.getCreatedAt());

            BigDecimal avg = IFeedbackRepository.findAverageRatingByTourId(tour.getTourId());
            resp.setRating(avg != null
                    ? avg.setScale(1, RoundingMode.HALF_UP)
                    : BigDecimal.valueOf(5.0));

            long count = IBookingRepository.countByTour_TourId(tour.getTourId());
            resp.setBookedCustomerCount((int) count);

            resp.setMyTour(true);

            return resp;
        }).toList();
    }

    public Tour createTour(TourCreateRequest request, String userId) {
        Tour tour = tourMapper.toEntity(request);

        User creator = iUserRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found!"));

        if (!"SUB_COMPANY".equalsIgnoreCase(creator.getRole())) {
            throw new AppException(ErrorCode.NOT_SUBCOMPANY, "User is not a SUB_COMPANY!");
        }

        Place place = iPlaceRepository.findById(request.getPlace())
                .orElseThrow(() -> new AppException(ErrorCode.PLACE_NOT_FOUND, "Place not found!"));

        Category category = iCategoryRepository.findById(request.getCategory())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND, "Category not found!"));

        tour.setManageBy(creator);
        tour.setPlace(place);
        tour.setCategory(category);
        tour.setCreatedAt(LocalDateTime.now());
        tour.setUpdatedAt(LocalDateTime.now());

        // 1. Lưu tour để có tourId
        tour = itourRepository.save(tour);
        // Lưu activityIds cho tour
        if (request.getActivityIds() != null) {
            for (String activityId : request.getActivityIds()) {
                Activity activity = iActivityRepository.findById(activityId)
                        .orElseThrow(() -> new AppException(ErrorCode.ACTIVITY_NOT_FOUND, "Activity not found!"));
                TourActivityId taId = new TourActivityId(tour.getTourId(), activityId);
                TourActivity ta = TourActivity.builder()
                        .id(taId)
                        .tour(tour)
                        .activity(activity)
                        .build();
                iTourActivityRepository.save(ta);
            }
        }

        // Lưu serviceIds cho tour
        if (request.getServiceIds() != null) {
            for (String serviceId : request.getServiceIds()) {
                Services service = iServicesRepository.findById(serviceId)
                        .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND, "Service not found!"));
                TourServiceId tsId = new TourServiceId(tour.getTourId(), serviceId);
                TourServices ts = TourServices.builder()
                        .id(tsId)
                        .tour(tour)
                        .service(service)
                        .build();
                iTourServicesRepository.save(ts);
            }
        }
        // 2. Xử lý tạo ngày khởi hành (start dates) và mapping
        String startDateStr = request.getStartDate(); // VD: "2025-07-14 08:00:00"
        int repeatTimes = request.getRepeatTimes();
        int repeatCycle = request.getRepeatCycle(); // số ngày chu kỳ lặp (mới thêm)

        if (startDateStr != null && !startDateStr.isEmpty()
                && repeatTimes > 0 && repeatCycle > 0) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime startDate = LocalDateTime.parse(startDateStr, formatter);

            createTourStarts(tour.getTourId(), startDate, repeatTimes, repeatCycle);
        }

        return tour;
    }




    public List<TourResponse> getToursByPlaceName(String placeName) {
        return tourRepository.findByPlace_PlaceNameIgnoreCase(placeName)
                .stream()
                .map(tourMapper::toResponse)
                .toList();
    }

    private TourResponse convertToResponse(Tour tour) {
        return TourResponse.builder()
                .tourId(tour.getTourId())
                .tourName(tour.getTourName())
                .description(tour.getDescription())
                .price(tour.getPrice())
                .duration(tour.getDuration())
                .minPeople(tour.getMinPeople())
                .maxPeople(tour.getMaxPeople())
                .touristNumberAssigned(tour.getTouristNumberAssigned())
                .thumbnail(tour.getThumbnail())
                .status(tour.getStatus())
                .placeName(tour.getPlace().getPlaceName())
                .categoryName(tour.getCategory().getCategoryName())
                .rating(BigDecimal.valueOf(5.0)) // hoặc tính trung bình nếu cần
                .createdByUserName(tour.getManageBy().getUserId())
                .bookedCustomerCount(0) // nếu chưa có booking, để mặc định
                .build();
    }

    public List<TourResponse> getAllTours() {
        return itourRepository.findAll().stream()
                .map(this::convertToResponse)
                .toList();
    }

    /**
     * So sánh tour: trả về tối đa 4 tour cùng lúc.
     */
    public List<TourResponse> compareTours(List<String> tourIds) {
        // 1) Validate số lượng và trùng lặp
        if (tourIds == null
                || tourIds.size() < 2
                || tourIds.size() > 4
                || tourIds.stream().distinct().count() != tourIds.size()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Feedback không hợp lệ và đã bị xoá!");
        }

        // 2) Lấy danh sách Tour từ DB
        List<Tour> tours = tourRepository.findAllByTourIdIn(tourIds);
        if (tours.size() != tourIds.size()) {
            throw new AppException(ErrorCode.TOUR_NOT_FOUND, "Feedback không hợp lệ và đã bị xoá!");
        }

        // 3) Map Tour → DTO và giữ nguyên thứ tự theo list tourIds
        Map<String, Tour> tourMap = tours.stream()
                .collect(Collectors.toMap(Tour::getTourId, Function.identity()));

        return tourIds.stream()
                .map(id -> {
                    Tour tour = tourMap.get(id);
                    // (chỉ phòng trường hợp, thực tế sẽ luôn có vì đã kiểm size phía trên)
                    if (tour == null) {
                        throw new AppException(ErrorCode.TOUR_NOT_FOUND, "Feedback không hợp lệ và đã bị xoá!");
                    }
                    return tourMapper.toResponse(tour);
                })
                .toList();
    }

    public TourResponse getTourById(String tourId) {
        Tour tour = itourRepository.findById(tourId)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND, "..."));
        TourResponse response = tourMapper.toResponse(tour);

        // ===== Lấy activities =====
        List<TourActivity> tourActivities = iTourActivityRepository.findByTour_TourId(tourId);
        List<TourResponse.ActivityDTO> activities = tourActivities.stream().map(ta -> {
            Activity a = ta.getActivity();
            TourResponse.ActivityDTO dto = new TourResponse.ActivityDTO();
            dto.setActivityId(a.getActivityId());
            dto.setName(a.getActivityName());
            return dto;
        }).toList();
        response.setActivities(activities);

        // ===== Lấy services =====
        List<TourServices> tourServices = iTourServicesRepository.findByTour_TourId(tourId);
        List<TourResponse.ServiceDTO> services = tourServices.stream().map(ts -> {
            Services s = ts.getService();
            TourResponse.ServiceDTO dto = new TourResponse.ServiceDTO();
            dto.setServiceId(s.getServiceId());
            dto.setName(s.getServiceName());
            return dto;
        }).toList();
        response.setServices(services);

        // ===== Lấy ngày startDate active gần nhất =====
        Optional<LocalDateTime> startDateOpt = iToursStartMappingRepository.findFirstActiveStartDateByTourId(tourId);
        startDateOpt.ifPresent(response::setStartDate);

        return response;
    }




    /**
     * Lấy danh sách TourResponse theo list id
     */
    public List<TourResponse> getToursByIds(List<String> ids) {
        if (ids == null || ids.isEmpty())
            return List.of();
        List<Tour> tours = tourRepository.findAllByTourIdIn(ids);
        return tours.stream().map(tourMapper::toResponse).toList();
    }

    @Transactional
    public void deleteTour(String tourId) {
        // 1️⃣ Kiểm tra tour có tồn tại?
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new EntityNotFoundException("Tour not found: " + tourId));

        // 2️⃣ Xóa tất cả feedback liên quan
        iFeedbackRepository.deleteByTour_TourId(tourId);

        // 3️⃣ Xóa tour
        tourRepository.delete(tour);
    }

    public List<LocalDateTime> getAllStartDatesByTourId(String tourId) {
        // 1. Lấy danh sách ngày từ repository
        List<LocalDateTime> dates = iToursStartMappingRepository
                .findAllActiveStartDatesByTourId(tourId);

        // 2. Nếu rỗng thì ném exception với mã TOUR_START_DATE_NOT_FOUND
        if (dates.isEmpty()) {
            throw new AppException(
                    ErrorCode.TOUR_START_DATE_NOT_FOUND,
                    "Không có lịch khởi hành cho tourId=" + tourId
            );
        }

        // 3. Ngược lại trả về danh sách
        return dates;
    }

    public void createTourStarts(String tourId, LocalDateTime startDate, int repeatTimes, int repeatCycle) {
        Tour tour = itourRepository.findById(tourId)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND, "Tour not found!"));

        for (int i = 0; i < repeatTimes; i++) {
            LocalDateTime currentStart = startDate.plusDays(i * repeatCycle);

            ToursStart start = new ToursStart();
            start.setStartDate(currentStart);
            start.setIsActive(true);
            start = toursStartRepository.save(start);

            ToursStartMappingId mappingId = new ToursStartMappingId();
            mappingId.setTourId(tour.getTourId());
            mappingId.setStartId(start.getStartId());

            ToursStartMapping mapping = new ToursStartMapping();
            mapping.setId(mappingId);
            mapping.setTour(tour);
            mapping.setStart(start);

            toursStartMappingRepository.save(mapping);
        }
    }

    @Transactional
    public void disableTour(String tourId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new EntityNotFoundException("Tour not found"));
        tour.setStatus("INACTIVE");
        tour.setUpdatedAt(LocalDateTime.now());
        tourRepository.save(tour);
    }

    @Transactional
    public void enableTour(String tourId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new EntityNotFoundException("Tour not found"));
        tour.setStatus("ACTIVE");
        tour.setUpdatedAt(LocalDateTime.now());
        tourRepository.save(tour);
    }


    @Transactional
    public void updateTour(String tourId, TourUpdateRequest req, String bearerToken) {
        String userId = jwtUtil.extractUserId(bearerToken.substring(7));
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND, "Tour not found!"));

        if (!tour.getManageBy().getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "You are not allowed to edit this tour!");
        }

        // Update các field cơ bản
        tour.setTourName(req.getTourName());
        tour.setDescription(req.getDescription());
        tour.setPrice(req.getPrice());
        tour.setDuration(req.getDuration());
        tour.setMinPeople(req.getMinPeople());
        tour.setMaxPeople(req.getMaxPeople());
        tour.setStatus(req.getStatus());
        tour.setThumbnail(req.getThumbnail());
        tour.setUpdatedAt(LocalDateTime.now());

        // CHỈ update category nếu FE vẫn gửi
        if (req.getCategory() != null) {
            Category category = iCategoryRepository.findById(req.getCategory())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND, "Category not found!"));
            tour.setCategory(category);
        }

        // ============= XỬ LÝ NGÀY KHỞI HÀNH ==============
        if (req.getStartDate() != null && !req.getStartDate().isEmpty()) {
            // Xóa hết mapping & start cũ của tour này (nếu có)
            List<ToursStartMapping> oldMappings = toursStartMappingRepository.findAllByTour_TourId(tourId);
            for (ToursStartMapping mapping : oldMappings) {
                String startId = mapping.getStart().getStartId();
                toursStartMappingRepository.delete(mapping);
                toursStartRepository.deleteById(startId);
            }
            // Thêm start mới
            ToursStart newStart = ToursStart.builder()
                    .startDate(LocalDateTime.parse(req.getStartDate()))
                    .isActive(true)
                    .build();
            ToursStart savedStart = toursStartRepository.save(newStart);

            ToursStartMapping mapping = ToursStartMapping.builder()
                    .id(new ToursStartMappingId(tourId, savedStart.getStartId()))
                    .tour(tour)
                    .start(savedStart)
                    .build();
            toursStartMappingRepository.save(mapping);
        }

        // ============= XỬ LÝ ACTIVITIES =============
        if (req.getActivityIds() != null) {
            iTourActivityRepository.deleteAllByTour_TourId(tourId);
            for (String activityId : req.getActivityIds()) {
                Activity activity = iActivityRepository.findById(activityId)
                        .orElseThrow(() -> new AppException(ErrorCode.ACTIVITY_NOT_FOUND, "Activity not found!"));
                TourActivityId taId = new TourActivityId(tourId, activityId);
                TourActivity mapping = TourActivity.builder()
                        .id(taId)
                        .tour(tour)
                        .activity(activity)
                        .build();
                iTourActivityRepository.save(mapping);
            }
        }

        // ============= XỬ LÝ SERVICES =============
        if (req.getServiceIds() != null) {
            iTourServicesRepository.deleteAllByTour_TourId(tourId);
            for (String serviceId : req.getServiceIds()) {
                Services service = iServicesRepository.findById(serviceId)
                        .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND, "Service not found!"));
                TourServiceId tsId = new TourServiceId(tourId, serviceId);
                TourServices mapping = TourServices.builder()
                        .id(tsId)
                        .tour(tour)
                        .service(service)
                        .build();
                iTourServicesRepository.save(mapping);
            }
        }

        tourRepository.save(tour);
    }

    public List<TourResponse> getAllDraftTours() {
        return tourRepository.findByStatusIgnoreCase("DRAFT")
                .stream()
                .map(tour -> new TourResponse(
                        tour.getTourId(),
                        tour.getTourName(),
                        tour.getDescription(),
                        tour.getDuration(),
                        tour.getPrice(),
                        tour.getMinPeople(),
                        tour.getMaxPeople(),
                        tour.getTouristNumberAssigned(),
                        tour.getThumbnail(),
                        tour.getStatus(),
                        tour.getPlace() != null ? tour.getPlace().getPlaceName() : null,
                        tour.getCategory() != null ? tour.getCategory().getCategoryName() : null,
                        tour.getCreatedAt(),
                        null, // startDate (nếu có trường thì thay)
                        null, // activities (tùy nếu cần map sang DTO)
                        null, // services
                        BigDecimal.valueOf(5.0), // rating mặc định
                        tour.getManageBy() != null ? tour.getManageBy().getUserName() : null,
                        null, // bookedCustomerCount (nếu có tính)
                        null, // managementBy (nếu cần map User -> UserResponse)
                        false // myTour
                ))
                .collect(Collectors.toList());
    }

    public void updateStatus(String tourId, String status) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new RuntimeException("Tour not found!"));
        tour.setStatus(status);
        tourRepository.save(tour);
    }



}