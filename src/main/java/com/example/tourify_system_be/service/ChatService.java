package com.example.tourify_system_be.service;

import com.example.tourify_system_be.dto.request.ChatRequest;
import com.example.tourify_system_be.dto.response.TourResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChatService {
    private final ChatClient chatClient;
    private final TourService tourService;

    // Bắt cả "tour", "ở", "tại", "đến", "tới", "du lịch", "đi", bỏ qua "và"/"có", dừng trước giá…
    private static final Pattern PLACE_PATTERN = Pattern.compile(
            "(?:(?:tour|ở|tại|đến|tới|du lịch|đi(?: du lịch)?))?\\s*" +  // prefix tuỳ chọn
                    "([\\p{L} ]+?)" +                                          // tên place (lazy)
                    "\\s*(?:(?:và|có)\\s*)*" +                                 // bỏ qua "và", "có"
                    "(?=giá|trên|dưới|từ|trong|[-–]|$)"                         // lookahead trước giá hoặc kết thúc
    );

    private static final Pattern PRICE_AROUND = Pattern.compile(
            "(?:giá\\s*)?(?:tầm|khoảng)\\s*([\\d.,]+)\\s*(triệu|tr|nghìn|nghìn\\s*đồng)?"
    );

    private static final Pattern PRICE_RANGE = Pattern.compile(
            "(?:giá\\s*)?(?:từ|trong khoảng)?\\s*" +
                    "([\\d.,]+)\\s*(triệu|tr|nghìn|nghìn đồng)?\\s*" +
                    "(?:đến|to|[-–])\\s*" +
                    "([\\d.,]+)\\s*(triệu|tr|nghìn|nghìn đồng)?"
    );
    private static final Pattern PRICE_BELOW = Pattern.compile(
            "(?:giá\\s*)?dưới\\s*([\\d.,]+)\\s*(triệu|tr|nghìn|nghìn đồng)?"
    );
    private static final Pattern PRICE_ABOVE = Pattern.compile(
            "(?:giá\\s*)?trên\\s*([\\d.,]+)\\s*(triệu|tr|nghìn|nghìn đồng)?"
    );

    public ChatService(ChatClient.Builder builder, TourService tourService) {
        this.chatClient  = builder.build();
        this.tourService = tourService;
    }

    public String chat(ChatRequest request) {
        String text = request.message().trim().toLowerCase();

        // 1) Lấy baseline theo place nếu và chỉ nếu có tour
        List<TourResponse> baseline = null;
        String placeName = null;
        Matcher mPlace = PLACE_PATTERN.matcher(text);
        if (mPlace.find()) {
            String p = mPlace.group(1).trim();
            List<TourResponse> tmp = tourService.getToursByPlaceName(p);
            if (!tmp.isEmpty()) {
                placeName = p;
                baseline  = tmp;
            }
        }

        // 2) Chọn nguồn: baseline nếu có, ngược lại tất cả
        List<TourResponse> source = baseline != null
                ? baseline
                : tourService.getAllTours();

        // Xử lý "tầm X" như khoảng [0.8X, 1.2X]
        Matcher mAround = PRICE_AROUND.matcher(text);
        if (mAround.find()) {
            BigDecimal center = parseAmount(mAround.group(1), mAround.group(2));
            BigDecimal delta  = center.multiply(new BigDecimal("0.2"));  // 20%
            BigDecimal min    = center.subtract(delta);
            BigDecimal max    = center.add(delta);

            List<TourResponse> list = source.stream()
                    .filter(t -> t.getPrice() != null
                            && t.getPrice().compareTo(min) >= 0
                            && t.getPrice().compareTo(max) <= 0)
                    .toList();

            return formatToursRange(list, min, max, placeName);
        }

        // 3) Xử lý khoảng giá A-B
        Matcher mRange = PRICE_RANGE.matcher(text);
        if (mRange.find()) {
            BigDecimal min = parseAmount(mRange.group(1), mRange.group(2));
            BigDecimal max = parseAmount(mRange.group(3), mRange.group(4));
            List<TourResponse> list = source.stream()
                    .filter(t -> t.getPrice() != null
                            && t.getPrice().compareTo(min) >= 0
                            && t.getPrice().compareTo(max) <= 0)
                    .toList();
            return formatToursRange(list, min, max, placeName);
        }

        // 4) Xử lý "giá dưới X"
        Matcher mBelow = PRICE_BELOW.matcher(text);
        if (mBelow.find()) {
            BigDecimal max = parseAmount(mBelow.group(1), mBelow.group(2));
            List<TourResponse> list = source.stream()
                    .filter(t -> t.getPrice() != null
                            && t.getPrice().compareTo(max) <= 0)
                    .toList();
            return formatTours(list, "dưới", max, placeName);
        }

        // 5) Xử lý "giá trên X"
        Matcher mAbove = PRICE_ABOVE.matcher(text);
        if (mAbove.find()) {
            BigDecimal min = parseAmount(mAbove.group(1), mAbove.group(2));
            List<TourResponse> list = source.stream()
                    .filter(t -> t.getPrice() != null
                            && t.getPrice().compareTo(min) >= 0)
                    .toList();
            return formatTours(list, "trên", min, placeName);
        }

        // 6) Nếu chỉ có place mà không có price
        if (baseline != null) {
            return formatToursByPlace(baseline, placeName);
        }

        // 7) Fallback: chuyển cho AI trả lời chung
        SystemMessage system = new SystemMessage("""
            Bạn là Tourify.AI, trợ lý chính thức của dự án Tourify.
            Trả lời trang trọng, lịch sự.
            Nếu không biết, chỉ cần nói “Xin lỗi, tôi không có thông tin đó.”
            """);
        UserMessage user = new UserMessage(request.message());
        Prompt prompt   = new Prompt(system, user);
        return chatClient.prompt(prompt).call().content();
    }

    // Helpers

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

    private String formatTours(List<TourResponse> tours,
                               String dir,
                               BigDecimal amt,
                               String place) {
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
                            .append(NumberFormat.getCurrencyInstance(vn).format(t.getPrice()))
                            .append("\n")
            );
        }
        return sb.toString();
    }

    private String formatToursRange(List<TourResponse> tours,
                                    BigDecimal min,
                                    BigDecimal max,
                                    String place) {
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
                            .append(NumberFormat.getCurrencyInstance(vn).format(t.getPrice()))
                            .append("\n")
            );
        }
        return sb.toString();
    }

    private String formatToursByPlace(List<TourResponse> tours, String place) {
        Locale vn = new Locale("vi","VN");
        String header = String.format("Các tour ở %s:", place);

        StringBuilder sb = new StringBuilder(header).append("\n\n");
        if (tours.isEmpty()) {
            sb.append("• Hiện tại không có tour nào ở ").append(place).append(".");
        } else {
            tours.forEach(t ->
                    sb.append("• ")
                            .append(t.getTourName())
                            .append(" — ")
                            .append(NumberFormat.getCurrencyInstance(vn).format(t.getPrice()))
                            .append("\n")
            );
        }
        return sb.toString();
    }
}
