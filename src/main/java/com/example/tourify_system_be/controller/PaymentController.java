package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.APIResponse;
import com.example.tourify_system_be.dto.request.CreatePaymentRequest;
import com.example.tourify_system_be.service.PayOSService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PayOSService payOSService;

    @PostMapping("/create")
    public APIResponse<?> createPayment(@RequestBody CreatePaymentRequest req) throws Exception {
        Map<String, String> result = payOSService.createPaymentLink(req);
        return APIResponse.<Map<String, String>>builder()
                .result(result)
                .build();
    }
}


