package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.PlaceCreateRequest;
import com.example.tourify_system_be.dto.request.PlaceUpdateRequest;
import com.example.tourify_system_be.dto.response.PlaceResponse;
import com.example.tourify_system_be.entity.Place;
import com.example.tourify_system_be.mapper.PlaceMapper;
import com.example.tourify_system_be.repository.IPlaceRepository;
import com.example.tourify_system_be.security.CustomUserDetails;
import com.example.tourify_system_be.service.PlaceService;
import com.example.tourify_system_be.utils.AuthUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlaceController.class)
public class PlaceControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private PlaceService placeService;

        @MockBean
        private IPlaceRepository placeRepository;

        @MockBean
        private PlaceMapper placeMapper;

        @MockBean
        private AuthUtils authUtils;

        private ObjectMapper objectMapper;
        private String validToken;
        private CustomUserDetails validUserDetails;

        @BeforeEach
        void setUp() {
                objectMapper = new ObjectMapper();
                validToken = "valid-jwt-token";
                validUserDetails = new CustomUserDetails("user-id", "testuser", "SUB_COMPANY");

                // Mock AuthUtils for SUB_COMPANY role
                when(authUtils.validateSubCompanyToken(anyString())).thenReturn(validUserDetails);
        }

        @Test
        void testCreatePlace_Success() throws Exception {
                // Given
                PlaceCreateRequest request = PlaceCreateRequest.builder()
                                .placeName("Test Place")
                                .placeDescription("Test Description")
                                .image("test-image.jpg")
                                .gpsCoordinates("10.123,20.456")
                                .rating(new BigDecimal("4.5"))
                                .build();

                PlaceResponse response = PlaceResponse.builder()
                                .placeId("test-id")
                                .placeName("Test Place")
                                .placeDescription("Test Description")
                                .image("test-image.jpg")
                                .gpsCoordinates("10.123,20.456")
                                .rating(new BigDecimal("4.5"))
                                .build();

                when(placeService.createPlace(any(PlaceCreateRequest.class))).thenReturn(response);

                // When & Then
                mockMvc.perform(post("/api/place")
                                .header("Authorization", "Bearer " + validToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.result.placeId").value("test-id"))
                                .andExpect(jsonPath("$.result.placeName").value("Test Place"));
        }

        @Test
        void testCreatePlace_UnauthorizedRole() throws Exception {
                // Given
                when(authUtils.validateSubCompanyToken(anyString()))
                                .thenThrow(new RuntimeException("Only SUB_COMPANY role is allowed"));

                PlaceCreateRequest request = PlaceCreateRequest.builder()
                                .placeName("Test Place")
                                .build();

                // When & Then
                mockMvc.perform(post("/api/place")
                                .header("Authorization", "Bearer " + validToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testCreatePlace_InvalidToken() throws Exception {
                // Given
                when(authUtils.validateSubCompanyToken(anyString()))
                                .thenThrow(new RuntimeException("Invalid token"));

                PlaceCreateRequest request = PlaceCreateRequest.builder()
                                .placeName("Test Place")
                                .build();

                // When & Then
                mockMvc.perform(post("/api/place")
                                .header("Authorization", "Bearer invalid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testGetPlaceById() throws Exception {
                // Given
                String placeId = "test-id";
                PlaceResponse response = PlaceResponse.builder()
                                .placeId(placeId)
                                .placeName("Test Place")
                                .placeDescription("Test Description")
                                .image("test-image.jpg")
                                .gpsCoordinates("10.123,20.456")
                                .rating(new BigDecimal("4.5"))
                                .build();

                when(placeService.getPlaceById(placeId)).thenReturn(response);

                // When & Then
                mockMvc.perform(get("/api/place/{placeId}", placeId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.result.placeId").value(placeId))
                                .andExpect(jsonPath("$.result.placeName").value("Test Place"));
        }

        @Test
        void testUpdatePlace_Success() throws Exception {
                // Given
                String placeId = "test-id";
                PlaceUpdateRequest request = PlaceUpdateRequest.builder()
                                .placeName("Updated Place")
                                .placeDescription("Updated Description")
                                .image("updated-image.jpg")
                                .gpsCoordinates("11.123,21.456")
                                .rating(new BigDecimal("4.8"))
                                .build();

                PlaceResponse response = PlaceResponse.builder()
                                .placeId(placeId)
                                .placeName("Updated Place")
                                .placeDescription("Updated Description")
                                .image("updated-image.jpg")
                                .gpsCoordinates("11.123,21.456")
                                .rating(new BigDecimal("4.8"))
                                .build();

                when(placeService.updatePlace(eq(placeId), any(PlaceUpdateRequest.class))).thenReturn(response);

                // When & Then
                mockMvc.perform(put("/api/place/{placeId}", placeId)
                                .header("Authorization", "Bearer " + validToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.result.placeId").value(placeId))
                                .andExpect(jsonPath("$.result.placeName").value("Updated Place"));
        }

        @Test
        void testDeletePlace_Success() throws Exception {
                // Given
                String placeId = "test-id";
                doNothing().when(placeService).deletePlace(placeId);

                // When & Then
                mockMvc.perform(delete("/api/place/{placeId}", placeId)
                                .header("Authorization", "Bearer " + validToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(1000))
                                .andExpect(jsonPath("$.message").value("Place deleted successfully"));
        }

        @Test
        void testDeletePlace_UnauthorizedRole() throws Exception {
                // Given
                when(authUtils.validateSubCompanyToken(anyString()))
                                .thenThrow(new RuntimeException("Only SUB_COMPANY role is allowed"));

                String placeId = "test-id";

                // When & Then
                mockMvc.perform(delete("/api/place/{placeId}", placeId)
                                .header("Authorization", "Bearer " + validToken))
                                .andExpect(status().isBadRequest());
        }
}