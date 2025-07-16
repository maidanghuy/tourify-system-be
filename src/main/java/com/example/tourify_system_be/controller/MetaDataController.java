package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.entity.Activity;
import com.example.tourify_system_be.entity.Services;
import com.example.tourify_system_be.repository.IActivityRepository;
import com.example.tourify_system_be.repository.IServicesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MetaDataController {
    private final IActivityRepository activityRepository;
    private final IServicesRepository servicesRepository;

    @GetMapping("/activities")
    public List<Activity> getAllActivities() {
        return activityRepository.findAll();
    }

    @GetMapping("/services")
    public List<Services> getAllServices() {
        return servicesRepository.findAll();
    }
}

