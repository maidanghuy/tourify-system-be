package com.example.tourify_system_be.service;

import com.example.tourify_system_be.dto.request.CreatePromotionRequest;
import com.example.tourify_system_be.dto.response.PromotionResponse;
import com.example.tourify_system_be.entity.Promotion;
import com.example.tourify_system_be.exception.AppException;
import com.example.tourify_system_be.exception.ErrorCode;
import com.example.tourify_system_be.mapper.PromotionMapper;
import com.example.tourify_system_be.repository.*;
import com.example.tourify_system_be.security.JwtUtil;
import com.example.tourify_system_be.entity.TokenAuthentication;
import com.example.tourify_system_be.entity.Tour;
import com.example.tourify_system_be.entity.User;
import com.example.tourify_system_be.entity.TourPromotion;
import com.example.tourify_system_be.entity.TourPromotionId;
import com.example.tourify_system_be.dto.request.UpdatePromotionRequest;
import org.apache.poi.ss.usermodel.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j

public class PromotionService {

    private final IPromotionRepository promotionRepository;
    private final IUserRepository userRepository;
    private final ITourRepository tourRepository;
    private final ITourPromotionRepository tourPromotionRepository;
    private final JwtUtil jwtUtil;
    private final ITokenAuthenticationRepository tokenAuthenticationRepository;
    private final PromotionMapper promotionMapper;

    public PromotionResponse createPromotion(CreatePromotionRequest request, String token) {
        // Ktr xem token có tồn tại không
        String jwt = token.replace("Bearer ", "");
        if (!jwtUtil.validateToken(jwt))
            throw new AppException(ErrorCode.SESSION_EXPIRED, "Hết phiên dăng nhập!");

        String userId = jwtUtil.extractUserId(jwt);
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy user!"));

        // Token hết hạn, hết phiên đăng nhập
        TokenAuthentication session = tokenAuthenticationRepository.findByTokenValue(jwt);
        if (session == null || !session.getIsUsed())
            throw new AppException(ErrorCode.SESSION_EXPIRED, "Phiên đăng nhập hết hạn");

        if (!creator.getRole().toUpperCase().equals("ADMIN") &&
                !creator.getRole().toUpperCase().equals("SUB_COMPANY"))
            throw new AppException(ErrorCode.ROLE_NOT_ALLOWED, "Không có quyền sử dụng cức năng này!");

        if (promotionRepository.findByCode(request.getCode()).isPresent())
            throw new AppException(ErrorCode.DUPLICATE_PROMOTION_CODE, "Mã code không được trùng");

        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new AppException(ErrorCode.INVALID_PROMOTION_TIME, "Ngày kết thúc không được trước ngày bắt đầu");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();

        if (request.getStartTime().isBefore(startOfToday)) {
            throw new AppException(ErrorCode.INVALID_TIME, "Ngày bắt đầu phải từ 00:00:00 của ngày hiện tại trở đi");
        }

        if (request.getEndTime().isBefore(now)) {
            throw new AppException(ErrorCode.INVALID_TIME, "Ngày kết thúc phải lớn hơn thời gian hiện tại");
        }

        String status = request.getStartTime().isAfter(LocalDateTime.now()) ? "inactive" : "active";

        Promotion promotion = Promotion.builder()
                .code(request.getCode())
                .quantity(request.getQuantity())
                .conditions(request.getConditions())
                .discountPercent(request.getDiscountPercent())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(status)
                .description(request.getDescription())
                .minPurchase(request.getMinPurchase())
                .createBy(creator)
                .build();

        Promotion t = promotionRepository.save(promotion);

        List<String> invalidTourIds = new ArrayList<>();

        if (request.getTourIds() != null && !request.getTourIds().isEmpty()) {
            for (UUID tourId : request.getTourIds()) {
                Tour tour = tourRepository.findById(tourId.toString())
                        .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND,
                                "Feedback không hợp lệ và đã bị xoá!"));

                // Tour-company chỉ được liên kết tour của công ty họ
                if (creator.getRole().equals("SUB_COMPANY") &&
                        !tour.getManageBy().getUserId().equals(creator.getUserId())) {
                    invalidTourIds.add(tourId.toString());
                    continue; // vẫn skip nhưng ghi nhận lỗi
                }

                TourPromotionId id = new TourPromotionId(tourId.toString(), promotion.getPromotionId());
                TourPromotion tourPromotion = TourPromotion.builder()
                        .id(id)
                        .tour(tour)
                        .promotion(promotion)
                        .build();

                tourPromotionRepository.save(tourPromotion);
            }

            // Sau khi xử lý xong, nếu có lỗi thì báo lỗi
            if (!invalidTourIds.isEmpty()) {
                throw new RuntimeException(
                        "You do not have permission to apply the promotion to the following tours: " + invalidTourIds);
            }
        }
        return promotionMapper.toPromotionResponse(t);
    }

    // Scheduler tích hợp: tự động chuyển promotion sang inactive khi promotion đc
    // tạo từ trc đến thời gian bắt đầu hoạt động
    @Scheduled(cron = "0 * * * * *") // chạy mỗi phút
    public void activatePromotionsAtStartTime() {
        List<Promotion> upcomingPromos = promotionRepository.findAllByStatus("inactive");
        for (Promotion promo : upcomingPromos) {
            LocalDateTime now = LocalDateTime.now();
            // Kiểm tra: bây giờ nằm trong khoảng thời gian hiệu lực
            if (!promo.getStartTime().isAfter(now) && promo.getEndTime().isAfter(now)) {
                promo.setStatus("active");
                promotionRepository.save(promo);
                log.info("✅ Promotion '{}' đã được kích hoạt", promo.getCode());
            }
        }
    }

    // Scheduler tích hợp: tự động chuyển promotion hết hạn sang inactive
    @Scheduled(cron = "0 * * * * *") // chạy mỗi phút
    public void updateExpiredPromotions() {
        List<Promotion> activePromotions = promotionRepository.findAllByStatus("active");
        for (Promotion promo : activePromotions) {
            if (!promo.getEndTime().isAfter(LocalDateTime.now())) {
                promo.setStatus("inactive");
                promotionRepository.save(promo);
                log.info("Promotion '{}' has expired and has been set to inactive", promo.getCode());
            }
        }
    }

    public List<PromotionResponse> getActivePromotionsByTour(String tourId) {
        List<Promotion> promotions = tourPromotionRepository.findActivePromotionsByTourId(tourId);
        return promotions.stream()
                .map(promotionMapper::toPromotionResponse)
                .toList();
    }

    // Update Promotion
    @Transactional
    public PromotionResponse editPromotion(String promotionId, UpdatePromotionRequest request, String token) {
        String jwt = token.replace("Bearer ", "");
        if (!jwtUtil.validateToken(jwt))
            throw new AppException(ErrorCode.SESSION_EXPIRED, "Phiên đăng nhập hết hạn!");

        String userId = jwtUtil.extractUserId(jwt);
        User editor = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng!"));

        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND, "Không tìm thấy promotion!"));

        boolean isAdmin = editor.getRole().equalsIgnoreCase("ADMIN");
        boolean isSubCompany = editor.getRole().equalsIgnoreCase("SUB_COMPANY");
        boolean isCreator = promotion.getCreateBy().getUserId().equals(userId);

        if (!(isAdmin || (isSubCompany && isCreator))) {
            throw new AppException(ErrorCode.PROMOTION_FORBIDDEN, "Không có quyền chỉnh sửa promotion này!");
        }

        // Kiểm tra code nếu user muốn sửa code
        if (!promotion.getCode().equals(request.getCode())) {
            if (promotionRepository.findByCode(request.getCode()).isPresent()) {
                throw new AppException(ErrorCode.DUPLICATE_PROMOTION_CODE, "Mã code không được trùng!");
            }
            promotion.setCode(request.getCode());
        }

        // Kiểm tra thời gian
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new AppException(ErrorCode.INVALID_PROMOTION_TIME, "Ngày kết thúc không được trước ngày bắt đầu");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        if (request.getStartTime().isBefore(startOfToday)) {
            throw new AppException(ErrorCode.INVALID_TIME, "Ngày bắt đầu phải từ 00:00:00 của ngày hiện tại trở đi");
        }
        if (request.getEndTime().isBefore(now)) {
            throw new AppException(ErrorCode.INVALID_TIME, "Ngày kết thúc phải lớn hơn thời gian hiện tại");
        }

        // Update các field
        promotion.setQuantity(request.getQuantity());
        promotion.setConditions(request.getConditions());
        promotion.setDiscountPercent(request.getDiscountPercent());
        promotion.setStartTime(request.getStartTime());
        promotion.setEndTime(request.getEndTime());
        promotion.setDescription(request.getDescription());
        promotion.setMinPurchase(request.getMinPurchase());

        // -------- Update danh sách tour liên kết ----------
        if (request.getTourIds() != null) {
            // Xoá hết liên kết cũ
            tourPromotionRepository.deleteByPromotion(promotion);

            List<String> invalidTourIds = new java.util.ArrayList<>();
            for (UUID tourId : request.getTourIds()) {
                Tour tour = tourRepository.findById(tourId.toString())
                        .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND,
                                "Không tìm thấy tour với id: " + tourId));

                // Nếu là sub-company thì chỉ được liên kết tour của mình
                if (isSubCompany && !tour.getManageBy().getUserId().equals(editor.getUserId())) {
                    invalidTourIds.add(tourId.toString());
                    continue;
                }
                TourPromotionId tpId = new TourPromotionId(tourId.toString(), promotion.getPromotionId());
                TourPromotion tp = TourPromotion.builder()
                        .id(tpId)
                        .tour(tour)
                        .promotion(promotion)
                        .build();
                tourPromotionRepository.save(tp);
            }
            if (!invalidTourIds.isEmpty()) {
                throw new RuntimeException(
                        "You do not have permission to apply the promotion to the following tours: " + invalidTourIds);
            }
        }

        promotionRepository.save(promotion);
        return promotionMapper.toPromotionResponse(promotion);
    }

    // Xóa Promotion
    @Transactional
    public void deletePromotions(List<String> promotionIds, String token) {
        String jwt = token.replace("Bearer ", "");
        if (!jwtUtil.validateToken(jwt))
            throw new AppException(ErrorCode.SESSION_EXPIRED, "Phiên đăng nhập hết hạn!");

        String userId = jwtUtil.extractUserId(jwt);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng!"));

        for (String promotionId : promotionIds) {
            Promotion promotion = promotionRepository.findById(promotionId)
                    .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND,
                            "Không tìm thấy promotion với id: " + promotionId));

            boolean isAdmin = user.getRole().equalsIgnoreCase("ADMIN");
            boolean isSubCompany = user.getRole().equalsIgnoreCase("SUB_COMPANY");
            boolean isCreator = promotion.getCreateBy().getUserId().equals(userId);

            if (isAdmin) {

            } else if (isSubCompany && isCreator) {
                // subcompany chỉ được phép xóa promotion do chính họ tạo ra
            } else {
                throw new AppException(ErrorCode.ROLE_NOT_ALLOWED,
                        "Bạn không có quyền xóa promotion có id: " + promotionId);
            }

            tourPromotionRepository.deleteByPromotion(promotion);

            promotionRepository.delete(promotion);
        }
    }

    //List Promotion theo role (cả active và inactive)
    public List<PromotionResponse> listPromotions(String token) {
        String jwt = token.replace("Bearer ", "");
        if (!jwtUtil.validateToken(jwt))
            throw new AppException(ErrorCode.SESSION_EXPIRED, "Hết phiên đăng nhập!");

        String userId = jwtUtil.extractUserId(jwt);
        String role = jwtUtil.extractRole(jwt);

        List<Promotion> promotions;

        if ("ADMIN".equalsIgnoreCase(role)) {
            // Admin: lấy tất cả
            promotions = promotionRepository.findAll();
        } else if ("SUB_COMPANY".equalsIgnoreCase(role)) {
            // SubCompany: chỉ lấy promotions do mình tạo
            promotions = promotionRepository.findByCreateBy_UserId(userId);
        } else {
            throw new AppException(ErrorCode.ROLE_NOT_ALLOWED, "Bạn không có quyền xem danh sách promotions");
        }

        return promotions.stream()
                .map(promotionMapper::toPromotionResponse)
                .toList();
    }

    public List<Map<String, Object>> parsePromotionExcel(MultipartFile file) throws IOException {
        List<Map<String, Object>> promotions = new ArrayList<>();
        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();
            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, Object> data = new HashMap<>();
                data.put("code", getStringCellValue(row.getCell(0)));
                data.put("description", getStringCellValue(row.getCell(1)));
                data.put("quantity", getIntCellValue(row.getCell(2)));
                data.put("minPurchase", getIntCellValue(row.getCell(3)));
                data.put("minPurchaseDescription", getStringCellValue(row.getCell(4)));
                data.put("discountPercent", getIntCellValue(row.getCell(5)));
                data.put("startTime", getStringCellValue(row.getCell(6)));
                data.put("endTime", getStringCellValue(row.getCell(7)));
                data.put("conditions", row.getCell(8) != null ? getStringCellValue(row.getCell(8)) : "");
                promotions.add(data);
            }
        }
        return promotions;
    }

    private int getIntCellValue(Cell cell) {
        if (cell == null) return 0;
        return switch (cell.getCellType()) {
            case NUMERIC -> (int) cell.getNumericCellValue();
            case STRING -> {
                String val = cell.getStringCellValue().trim();
                if (val.isEmpty()) yield 0;
                yield Integer.parseInt(val.replaceAll("[^0-9]", "")); // loại ký tự thừa
            }
            default -> 0;
        };
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) return "";
        return cell.getCellType() == CellType.NUMERIC
                ? String.valueOf((int) cell.getNumericCellValue())
                : cell.getStringCellValue();
    }


}
