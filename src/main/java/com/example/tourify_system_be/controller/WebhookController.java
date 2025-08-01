package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.entity.*;
import com.example.tourify_system_be.repository.*;
import com.example.tourify_system_be.service.BookingTourService;
import com.example.tourify_system_be.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class WebhookController {


    private final PayOS payOS;
    private final BookingTourService bookingTourService;
    private final EmailService emailService;

    private final IPaymentOrderRefRepository orderRefRepository;
    private final IPaymentRepository paymentRepository;
    private final IPaymentMethodRepository paymentMethodRepository;
    private final IBookingTourRepository bookingTourRepository;
    private final IUserRepository userRepository;
    private final ITourRepository tourRepository;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String rawBody) {
//        NO DELETING THIS CODE
//        System.out.println("rawBody: " + rawBody);
//        return ResponseEntity.ok("Webhook xử lý thành công");

        try {
            // Parse JSON
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode webhook = (ObjectNode) objectMapper.readTree(rawBody);
            Webhook webhookObj = objectMapper.treeToValue(webhook, Webhook.class);

            // Xác minh chữ ký PayOS
            WebhookData data = payOS.verifyPaymentWebhookData(webhookObj);

            Long orderCode = data.getOrderCode();

            // Truy mapping từ orderCode
            PaymentOrderRef ref = orderRefRepository.findById(orderCode)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy orderCode trong hệ thống"));

            String userId = ref.getUserId();
            String bookingId = ref.getBookingId();
            System.out.println("Webhook hợp lệ: orderCode=" + orderCode + ", userId=" + userId + ", bookingId=" + bookingId);

            // Lấy BookingTour tương ứng (giả định bạn có bảng này)
            BookingTour booking = bookingTourRepository
                    .findByBookingId(bookingId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy booking cho user và tour"));
            booking.setStatus("SUCCESS");
            bookingTourRepository.save(booking);

            // Lấy phương thức thanh toán mặc định
            PaymentMethod defaultMethod = paymentMethodRepository
                    .findByMethodName("PayOS")
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phương thức thanh toán mặc định"));

            // Lưu Payment
            Payment payment = Payment.builder()
                    .booking(booking)
                    .paymentMethod(defaultMethod)
                    .amount(BigDecimal.valueOf(data.getAmount()))
                    .status("SUCCESS")
                    .paymentReference(data.getReference())
                    .payAt(LocalDateTime.parse(data.getTransactionDateTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .orderCode(data.getOrderCode())
                    .payerName(data.getCounterAccountName())
                    .payerAccountNumber(data.getCounterAccountNumber())
                    .currency(data.getCurrency())
                    .build();

            paymentRepository.save(payment);

            User user = userRepository.findByUserId(userId);
            Tour tour = tourRepository.findByTourId(booking.getTour().getTourId());
            emailService.sendBookingPAIDEmail(booking, tour, user);


            return ResponseEntity.ok("Webhook xử lý thành công");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook không hợp lệ");
        }
    }
}

