package com.example.tourify_system_be.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PaymentReturnController {

    @GetMapping("/payment-success")
    public String onSuccess(@RequestParam("code") String code,
                            @RequestParam("id") String transactionId,
                            @RequestParam("cancel") boolean cancel,
                            @RequestParam("status") String status,
                            @RequestParam("orderCode") long orderCode,
                            Model model) {
        // Có thể kiểm tra thêm status, code để xác thực
        model.addAttribute("code", code);
        model.addAttribute("transactionId", transactionId);
        model.addAttribute("cancel", cancel);
        model.addAttribute("status", status);
        model.addAttribute("orderCode", orderCode);
        return "payment-success";  // sẽ render src/main/resources/templates/payment-success.html
    }
}
