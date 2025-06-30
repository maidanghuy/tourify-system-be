package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.service.BookingTourService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class WebhookController {


    private final PayOS payOS;
    private final BookingTourService bookingTourService;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String rawBody) {
        // NO DELETING THIS CODE
        /*
            System.out.println("rawBody: " + rawBody);
            return ResponseEntity.ok("Webhook xử lý thành công");
        */
        try {

            System.out.println(rawBody);
            ObjectNode webhook = (ObjectNode) new ObjectMapper().readTree(rawBody);
            ObjectMapper objectMapper = new ObjectMapper();
            Webhook webhookObj = objectMapper.treeToValue(webhook, Webhook.class);

            WebhookData data = payOS.verifyPaymentWebhookData(webhookObj);

            // check orderCode
            System.out.println("Webhook hợp lệ cho orderCode: " + data.getOrderCode());

            if ("success".equalsIgnoreCase(data.getDesc())) {
//                bookingTourService.markAsPaid(data.getOrderCode());
            }

            return ResponseEntity.ok("Webhook xử lý thành công");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook không hợp lệ");
        }
    }
}

