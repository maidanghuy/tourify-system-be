package com.example.tourify_system_be.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubCompanyResponse {
    String userId;
    String companyName;          // user.userName
    String contactPerson;        // user.firstName + user.lastName
    String dob;                  // user.dob (format dd/MM/yyyy)
    String email;
    String hotline;              // user.phoneNumber
    String address;
    String website;
    String description;
    int totalToursCreated;
    int totalFollowers;
    int totalServedUsers;
    String avatar;
    String background;
}
