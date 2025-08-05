package com.example.tourify_system_be.service;

import com.example.tourify_system_be.dto.request.ChatRequest;
import com.example.tourify_system_be.dto.response.TourResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChatService {
    private final ChatClient chatClient;
    private final TourService tourService;
    private final GoogleDocsService gdocs;

    private static final Pattern MAX_PRICE_PATTERN = Pattern.compile(
            "(tour|chuyến đi)\\s+([\\p{L} ]+?)?\\s*(nào)?\\s*(đắt nhất|cao nhất|max|nhiều tiền nhất)"
    );
    private static final Pattern MIN_PRICE_PATTERN = Pattern.compile(
            "(tour|chuyến đi)\\s+([\\p{L} ]+?)?\\s*(nào)?\\s*(rẻ nhất|thấp nhất|min|ít tiền nhất)"
    );

    private static final Pattern ALL_TOURS_PATTERN = Pattern.compile(
            "(tất cả tour|danh sách tour|list all tours|show all tours)",
            Pattern.CASE_INSENSITIVE
    );


    private static final Pattern PLACE_PATTERN = Pattern.compile(
            "(?:(?:tour|ở|tại|đến|tới|du lịch|đi(?: du lịch)?))?\\s*"
                    + "([\\p{L} ]+?)"
                    + "\\s*(?:(?:và|có)\\s*)*"
                    + "(?=giá|trên|dưới|từ|trong|[-–]|$)"
    );
    private static final Pattern PRICE_AROUND = Pattern.compile(
            "(?:giá\\s*)?(?:tầm|khoảng)\\s*([\\d.,]+)\\s*(triệu|tr|nghìn|nghìn\\s*đồng)?"
    );
    private static final Pattern PRICE_RANGE = Pattern.compile(
            "(?:giá\\s*)?(?:từ|trong khoảng)?\\s*"
                    + "([\\d.,]+)\\s*(triệu|tr|nghìn|nghìn đồng)?\\s*"
                    + "(?:đến|to|[-–])\\s*"
                    + "([\\d.,]+)\\s*(triệu|tr|nghìn|nghìn đồng)?"
    );
    private static final Pattern PRICE_BELOW = Pattern.compile(
            "(?:giá\\s*)?dưới\\s*([\\d.,]+)\\s*(triệu|tr|nghìn|nghìn đồng)?"
    );
    private static final Pattern PRICE_ABOVE = Pattern.compile(
            "(?:giá\\s*)?trên\\s*([\\d.,]+)\\s*(triệu|tr|nghìn|nghìn đồng)?"
    );


    private static final Pattern ALL_BY_PLACE_PATTERN = Pattern.compile(
            "(?:danh sách|list|xem) (?:tất cả )?tour(?: ở| tại)?\\s*([\\p{L} ]+)",
            Pattern.CASE_INSENSITIVE
    );


    public ChatService(ChatClient.Builder builder, TourService tourService, GoogleDocsService gdocs) {
        this.chatClient  = builder.build();
        this.tourService = tourService;
        this.gdocs       = gdocs;
    }

    public String chat(ChatRequest request) {
        String text = request.message().trim().toLowerCase();

        try {
            gdocs.appendText(String.format("[%s] USER: %s", LocalDateTime.now(), text));
        } catch (Exception e) {
            // log error
            e.printStackTrace();
        }

        // Xác định địa điểm (nếu có) và chỉ lấy khi có tour thật
        String placeName = extractPlaceName(text);
        List<TourResponse> baseline = (placeName != null)
                ? tourService.getToursByPlaceName(placeName)
                : null;
        if (baseline != null && baseline.isEmpty()) {
            placeName = null;
            baseline  = null;
        }

        List<TourResponse> source = (baseline != null)
                ? baseline
                : tourService.getAllTours();

        // 1. Check tour đắt nhất có place nằm giữa
        Matcher mMax = MAX_PRICE_PATTERN.matcher(text);
        if (mMax.find()) {
            String place = mMax.group(2) != null ? mMax.group(2).trim() : null;
            List<TourResponse> base = (place != null && !place.isEmpty())
                    ? tourService.getToursByPlaceName(place)
                    : tourService.getAllTours();
            if (base == null || base.isEmpty()) {
                return "Không tìm thấy tour nào phù hợp.";
            }
            return handleTourMaxPrice(base, place);
        }

        // 2. Check tour rẻ nhất có place nằm giữa
        Matcher mMin = MIN_PRICE_PATTERN.matcher(text);
        if (mMin.find()) {
            String place = mMin.group(2) != null ? mMin.group(2).trim() : null;
            List<TourResponse> base = (place != null && !place.isEmpty())
                    ? tourService.getToursByPlaceName(place)
                    : tourService.getAllTours();
            if (base == null || base.isEmpty()) {
                return "Không tìm thấy tour nào phù hợp.";
            }
            return handleTourMinPrice(base, place);
        }

        Matcher mAll = ALL_TOURS_PATTERN.matcher(text);
        if (mAll.find()) {
            List<TourResponse> all = tourService.getAllTours();
            return formatAllTours(all);
        }

        // 5. Check “list all tours at <place>”
        Matcher mList = ALL_BY_PLACE_PATTERN.matcher(text);
        if (mList.find()) {
            String place = mList.group(1).trim();
            List<TourResponse> list = tourService.getToursByPlaceName(place);
            if (list == null || list.isEmpty()) {
                return String.format("Hiện tại không có tour nào ở %s.", place);
            }
            // Ta đã có helper formatToursByPlace(baseline, place)
            return formatToursByPlace(list, place);
        }


        // Ưu tiên từng dạng câu hỏi
        if (isPriceAround(text)) return handlePriceAround(text, source, placeName);
        if (isPriceRange(text))  return handlePriceRange(text, source, placeName);
        if (isPriceBelow(text))  return handlePriceBelow(text, source, placeName);
        if (isPriceAbove(text))  return handlePriceAbove(text, source, placeName);
        if (baseline != null)    return handlePlaceOnly(baseline, placeName);

        // Nếu không match pattern nào: fallback AI
        return handleAI(request.message());
    }


    public String chatWithImage(MultipartFile file, String message){
        // Log user image message
        try {
            gdocs.appendText(String.format("[%s] USER[IMG]: %s", LocalDateTime.now(), message));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Media media = Media.builder()
                .mimeType(MimeTypeUtils.parseMimeType(file.getContentType()))
                .data(file.getResource())
                .build();


        ChatOptions chatOptions = ChatOptions.builder()
                .temperature(0D)
                .build();


        return chatClient.prompt()
                .options(chatOptions)
                .system("You are Tourify.AI")
                .user(promptUserSpec
                        -> promptUserSpec.media(media)
                        .text(message))
                .call()
                .content();
    }

    // ------------------ Các hàm phụ ----------------------

    private String extractPlaceName(String text) {
        Matcher mPlace = PLACE_PATTERN.matcher(text);
        if (mPlace.find()) {
            String place = mPlace.group(1).trim();
            // Chỉ trả về nếu có tour (tránh match nhầm "giá", "tour" ...)
            if (!tourService.getToursByPlaceName(place).isEmpty()) {
                return place;
            }
        }
        return null;
    }

    private boolean isPriceAround(String text) {
        return PRICE_AROUND.matcher(text).find();
    }
    private boolean isPriceRange(String text) {
        return PRICE_RANGE.matcher(text).find();
    }
    private boolean isPriceBelow(String text) {
        return PRICE_BELOW.matcher(text).find();
    }
    private boolean isPriceAbove(String text) {
        return PRICE_ABOVE.matcher(text).find();
    }

    private String handleTourMaxPrice(List<TourResponse> source, String place) {
        Locale vn = new Locale("vi","VN");
        TourResponse max = source.stream()
                .filter(t -> t.getPrice() != null)
                .max((a, b) -> a.getPrice().compareTo(b.getPrice()))
                .orElse(null);
        if (max == null) {
            return "Không tìm thấy tour nào phù hợp.";
        }
        String header = place != null
                ? String.format("Tour đắt nhất ở %s:", capitalize(place))
                : "Tour đắt nhất:";
        return String.format("%s\n\n• %s — %s ₫\n  Link: %s",
                header, max.getTourName(),
                NumberFormat.getNumberInstance(vn).format(max.getPrice()),
                buildTourLink(max.getTourId())
        );
    }

    private String handleTourMinPrice(List<TourResponse> source, String place) {
        Locale vn = new Locale("vi","VN");
        TourResponse min = source.stream()
                .filter(t -> t.getPrice() != null)
                .min((a, b) -> a.getPrice().compareTo(b.getPrice()))
                .orElse(null);
        if (min == null) {
            return "Không tìm thấy tour nào phù hợp.";
        }
        String header = place != null
                ? String.format("Tour rẻ nhất ở %s:", capitalize(place))
                : "Tour rẻ nhất:";
        return String.format("%s\n\n• %s — %s ₫\n  Link: %s",
                header, min.getTourName(),
                NumberFormat.getNumberInstance(vn).format(min.getPrice()),
                buildTourLink(min.getTourId())
        );
    }

    private String handlePriceAround(String text, List<TourResponse> source, String place) {
        Matcher m = PRICE_AROUND.matcher(text);
        if (!m.find()) return "";
        BigDecimal center = parseAmount(m.group(1), m.group(2));
        BigDecimal delta  = center.multiply(new BigDecimal("0.2"));  // 20%
        BigDecimal min    = center.subtract(delta);
        BigDecimal max    = center.add(delta);

        List<TourResponse> list = source.stream()
                .filter(t -> t.getPrice() != null
                        && t.getPrice().compareTo(min) >= 0
                        && t.getPrice().compareTo(max) <= 0)
                .toList();

        return formatToursRange(list, min, max, place);
    }

    private String handlePriceRange(String text, List<TourResponse> source, String place) {
        Matcher m = PRICE_RANGE.matcher(text);
        if (!m.find()) return "";
        BigDecimal min = parseAmount(m.group(1), m.group(2));
        BigDecimal max = parseAmount(m.group(3), m.group(4));
        List<TourResponse> list = source.stream()
                .filter(t -> t.getPrice() != null
                        && t.getPrice().compareTo(min) >= 0
                        && t.getPrice().compareTo(max) <= 0)
                .toList();
        return formatToursRange(list, min, max, place);
    }

    private String handlePriceBelow(String text, List<TourResponse> source, String place) {
        Matcher m = PRICE_BELOW.matcher(text);
        if (!m.find()) return "";
        BigDecimal max = parseAmount(m.group(1), m.group(2));
        List<TourResponse> list = source.stream()
                .filter(t -> t.getPrice() != null
                        && t.getPrice().compareTo(max) <= 0)
                .toList();
        return formatTours(list, "dưới", max, place);
    }

    private String handlePriceAbove(String text, List<TourResponse> source, String place) {
        Matcher m = PRICE_ABOVE.matcher(text);
        if (!m.find()) return "";
        BigDecimal min = parseAmount(m.group(1), m.group(2));
        List<TourResponse> list = source.stream()
                .filter(t -> t.getPrice() != null
                        && t.getPrice().compareTo(min) >= 0)
                .toList();
        return formatTours(list, "trên", min, place);
    }

    private String handlePlaceOnly(List<TourResponse> baseline, String place) {
        return formatToursByPlace(baseline, place);
    }

    private String handleAI(String message) {
        SystemMessage system = new SystemMessage("""
            Bạn là Tourify.AI, trợ lý chính thức của dự án Tourify.
            Trả lời trang trọng, lịch sự.
            Nếu không biết, chỉ cần nói “Xin lỗi, tôi không có thông tin đó.”
            """);
        UserMessage user = new UserMessage(message);
        Prompt prompt   = new Prompt(system, user);
        return chatClient.prompt(prompt).call().content();
    }

    // ------- Format/Parse Helper -------
    private BigDecimal parseAmount(String raw, String unit) {
        String digits = raw.replaceAll("[.,]", "");
        BigDecimal val = new BigDecimal(digits);
        if (unit != null) {
            unit = unit.trim();
            if (unit.startsWith("triệu") || unit.equals("tr")) {
                val = val.multiply(BigDecimal.valueOf(1_000_000));
            } else if (unit.startsWith("nghìn")) {
                val = val.multiply(BigDecimal.valueOf(1_000));
            }
        }
        return val;
    }

    private String formatTours(List<TourResponse> tours, String dir, BigDecimal amt, String place) {
        Locale vn   = new Locale("vi","VN");
        String sAmt = NumberFormat.getNumberInstance(vn).format(amt) + "₫";
        String header = place != null
                ? String.format("Các tour ở %s giá %s %s:", place, dir, sAmt)
                : String.format("Các tour giá %s %s:", dir, sAmt);

        StringBuilder sb = new StringBuilder(header).append("\n\n");
        if (tours.isEmpty()) {
            sb.append("• Không tìm thấy tour nào phù hợp.");
        } else {
            tours.forEach(t ->
                    sb.append("• ")
                            .append(t.getTourName())
                            .append(" — ")
                            .append(NumberFormat.getNumberInstance(vn).format(t.getPrice()))
                            .append(" ₫\n")
                            .append("  <a href=\"")
                            .append(buildTourLink(t.getTourId()))
                            .append("\" target=\"_blank\">Xem chi tiết</a>")
                            .append("\n")
            );
        }
        return sb.toString();
    }

    private String formatToursRange(List<TourResponse> tours, BigDecimal min, BigDecimal max, String place) {
        Locale vn   = new Locale("vi","VN");
        String sMin = NumberFormat.getNumberInstance(vn).format(min) + "₫";
        String sMax = NumberFormat.getNumberInstance(vn).format(max) + "₫";
        String header = place != null
                ? String.format("Các tour ở %s từ %s đến %s:", place, sMin, sMax)
                : String.format("Các tour từ %s đến %s:", sMin, sMax);

        StringBuilder sb = new StringBuilder(header).append("\n\n");
        if (tours.isEmpty()) {
            sb.append("• Không tìm thấy tour nào phù hợp.");
        } else {
            tours.forEach(t ->
                    sb.append("• ")
                            .append(t.getTourName())
                            .append(" — ")
                            .append(NumberFormat.getNumberInstance(vn).format(t.getPrice()))
                            .append(" ₫\n")
                            .append("  <a href=\"")
                            .append(buildTourLink(t.getTourId()))
                            .append("\" target=\"_blank\">Xem chi tiết</a>")
                            .append("\n")
            );
        }
        return sb.toString();
    }

    private String formatToursByPlace(List<TourResponse> tours, String place) {
        Locale vn = new Locale("vi","VN");
        String header = String.format("Các tour ở %s:", capitalize(place));

        StringBuilder sb = new StringBuilder(header).append("\n\n");
        if (tours.isEmpty()) {
            sb.append("• Hiện tại không có tour nào ở ").append(capitalize(place)).append(".");
        } else {
            tours.forEach(t ->
                    sb.append("• ")
                            .append(t.getTourName())
                            .append(" — ")
                            .append(NumberFormat.getNumberInstance(vn).format(t.getPrice()))
                            .append(" ₫\n")
                            .append("  <a href=\"")
                            .append(buildTourLink(t.getTourId()))
                            .append("\" target=\"_blank\">Xem chi tiết</a>")
                            .append("\n")
            );
        }
        return sb.toString();
    }

    private String formatAllTours(List<TourResponse> tours) {
        if (tours == null || tours.isEmpty()) {
            return "Hiện tại không có tour nào trên hệ thống.";
        }
        StringBuilder sb = new StringBuilder("Danh sách tất cả tour:\n\n");
        Locale vn = new Locale("vi", "VN");
        NumberFormat fmt = NumberFormat.getNumberInstance(vn);

        for (TourResponse t : tours) {
            sb.append("• ")
                    .append(t.getTourName())
                    .append(" — ")
                    .append(NumberFormat.getNumberInstance(vn).format(t.getPrice()))
                    .append(" ₫\n")
                    .append("  <a href=\"")
                    .append(buildTourLink(t.getTourId()))
                    .append("\" target=\"_blank\">Xem chi tiết</a>")
                    .append("\n");
        }
        return sb.toString();
    }


    // Viết hoa chữ cái đầu mỗi từ (cho đẹp)
    private String capitalize(String text) {
        String[] parts = text.trim().split("\\s+");
        for (int i = 0; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                parts[i] = parts[i].substring(0,1).toUpperCase() + parts[i].substring(1);
            }
        }
        return String.join(" ", parts);
    }

    private String buildTourLink(String tourId) {

        return "https://ce9cd71506de.ngrok-free.app/tourify/tourDetail?id=" + tourId;
    }
}
