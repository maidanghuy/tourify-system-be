package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.entity.Category;
import com.example.tourify_system_be.repository.ICategoryRepository;
import com.example.tourify_system_be.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Controller
public class PageController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/")
    public String homePage() {
        return "landing";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * Landing page và home ("/").
     */
    @GetMapping({"/", "/landing"})
    public String landingPage(
            @RequestParam(required = false) String placeName,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) Integer duration,
            @RequestParam(required = false) Integer touristNumberAssigned,
            Model model
    ) {
        // Truyền lại giá trị filter để giữ trạng thái form
        model.addAttribute("placeName", placeName);
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("duration", duration);
        model.addAttribute("touristNumberAssigned", touristNumberAssigned);

        // Lấy danh sách categories active đã sắp xếp
        List<Category> categories = categoryService.getCategoriesByStatus("active");
        model.addAttribute("categories", categories);

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

    /**
     * Trang danh sách tour.
     */
    @GetMapping("/tour")
    public String tourListPage(
            @RequestParam(required = false) String placeName,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) Integer duration,
            @RequestParam(required = false) Integer touristNumberAssigned,
            Model model
    ) {
        model.addAttribute("placeName", placeName);
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("duration", duration);
        model.addAttribute("touristNumberAssigned", touristNumberAssigned);

        List<Category> categories = categoryService.getCategoriesByStatus("active");
        model.addAttribute("categories", categories);

        return "tour_list";
    }
  
    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "dashboard";
    }

    @GetMapping("/tourlistbyplace")
    public String tourListByPlacePage(@RequestParam(required = false) String placeName, Model model) {
        model.addAttribute("placeName", placeName); // để Thymeleaf có thể hiển thị tên địa điểm
        return "tourlistbyplace";
    }

    @GetMapping("/tourDetail")
    public  String tourDetailPage(){return "tour_detail";}

    @GetMapping("/user/favorites")
    public String userFavorites() {return "favorites";}

    @GetMapping("/tour/booking")
    public String bookingPage() {
        return "booking";
    }

    @GetMapping("/user/edit-account")
    public String editAccountPage() {
        return "edit_account";
    }
}

