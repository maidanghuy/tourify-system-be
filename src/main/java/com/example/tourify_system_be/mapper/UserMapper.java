package com.example.tourify_system_be.mapper;

import com.example.tourify_system_be.dto.request.UserUpdateRequest;
import com.example.tourify_system_be.dto.response.UserResponse;
import com.example.tourify_system_be.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User user);

    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
