package com.example.tourify_system_be.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubCompanyResponse {
    String companyName;
    String contactName;
    String dob;
    String email;
    String hotline;
    String address;
    String website;
    String description;
    int totalTours;
    long followerCount;
    long totalCustomersServed;
}