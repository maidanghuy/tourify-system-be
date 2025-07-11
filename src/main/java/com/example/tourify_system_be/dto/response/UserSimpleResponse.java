package com.example.tourify_system_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSimpleResponse {
    private String userId;
    private String userName;
    private String avatar;
}
