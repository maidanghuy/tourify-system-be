package com.example.tourify_system_be.service;


import com.example.tourify_system_be.config.PayOSProperties;
import com.example.tourify_system_be.dto.request.CreatePaymentRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;

import java.util.List;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
@RequiredArgsConstructor
public class PayOSService {

    PayOS payOS;
    PayOSProperties properties;

    public Map<String, String> createPaymentLink(CreatePaymentRequest req) throws Exception {
        long orderCode = System.currentTimeMillis();

        ItemData item = ItemData.builder()
                .name("Tourify Tour Booking")
                .quantity(1)
                .price(req.getAmount())
                .build();

        PaymentData paymentData = PaymentData.builder()
                .orderCode(orderCode)
                .amount(req.getAmount())
                .description(req.getDescription())
                .items(List.of(item))
                .cancelUrl(properties.getCancelUrl())
                .returnUrl(properties.getReturnUrl())
                .build();

        CheckoutResponseData resp = payOS.createPaymentLink(paymentData);

        return Map.of(
                "orderCode", String.valueOf(orderCode),
                "checkoutUrl", resp.getCheckoutUrl(),
                "qrCode", resp.getQrCode()
        );
    }
}

