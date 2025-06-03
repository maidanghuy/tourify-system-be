package com.example.tourify_system_be.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/landing")
    public String landingPage() {
        return "landing";
    }

    @GetMapping("/forgot_password")
    public String forgotPasswordPage() {
        return "forgot_password";
    }

    @GetMapping("/reset_password")
    public String restPasswordPage() {
        return "reset_password";
    }

    @GetMapping("/user/profile")
    public String profilePage() {
        return "profile";
    }
}

