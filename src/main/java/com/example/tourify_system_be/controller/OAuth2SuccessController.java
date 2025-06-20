package com.example.tourify_system_be.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OAuth2SuccessController {

    @GetMapping("/tourify/oauth/success")
    public String handleOAuth2Success() {
        return "oauth-success"; // Trả về view tên là oauth-success.html
    }
}