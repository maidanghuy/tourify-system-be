package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.UserUpdateRequest;
import com.example.tourify_system_be.entity.User;
import com.example.tourify_system_be.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")

public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("")
    public Iterable<User> getAllUsers(){
        return userService.getAllUsers();
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable("id") String id, @RequestBody UserUpdateRequest request){
        return userService.updateUser(id, request);
    }
    /*
    Sample JSON:
    {
          "userName": "johndoe",
          "firstName": "John",
          "lastName": "Doe",
          "phoneNumber": "0987654321",
          "gender": true,
          "address": "123 Main Street, City",
          "dob": "2004-08-31T00:00:00Z",
          "status": "active",
          "socialLink": "https://facebook.com/johndoe",
          "avatar": "https://example.com/avatar.jpg",
          "background": "https://example.com/background.jpg"
      }
    */

    @GetMapping("/{id}")
    public User getUserById(@PathVariable("id") String id){
        return userService.getUserById(id);
    }
}
