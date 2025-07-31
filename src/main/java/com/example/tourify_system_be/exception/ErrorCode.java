package com.example.tourify_system_be.exception;

import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    // 9999 - System errors
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),

    // 1000–1099: Authentication & Authorization
    INVALID_KEY(1001, "Invalid API key", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User already exists", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1003, "User not found", HttpStatus.NOT_FOUND),
    EMAIL_NOT_FOUND(1004, "Email not found", HttpStatus.NOT_FOUND),
    FAIL_TO_SEEN_EMAIL(1005, "Failed to mark email as seen", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(1006, "Invalid token", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED(1007, "Token has expired", HttpStatus.BAD_REQUEST),
    TOKEN_ALREADY_USED(1008, "Token has already been used", HttpStatus.BAD_REQUEST),
    PASSWORD_CONFIRMATION_MISMATCH(1009, "Password confirmation does not match", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(1010, "Invalid username or password", HttpStatus.BAD_REQUEST),
    FAIL_TO_SEND_REGISTRATION_TOKEN(1011, "Failed to send registration token", HttpStatus.BAD_REQUEST),
    PASSWORD_TOO_SHORT(1012, "Password must be at least 6 characters long", HttpStatus.BAD_REQUEST),
    WRONG_PASSWORD(1013, "Incorrect password", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1014, "Email already exists", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_USED(1015, "Email has already been used", HttpStatus.BAD_REQUEST),
    PHONE_ALREADY_USED(1016, "Phone number has already been used", HttpStatus.BAD_REQUEST),
    PLACE_NOT_FOUND(1017,"Place not found",HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_FOUND(1018,"Category not found",HttpStatus.BAD_REQUEST),
    NOT_SUBCOMPANY(1019,"Role isn't sub-company",HttpStatus.BAD_REQUEST),
    BOOKING_FORBIDDEN_ROLE(1020, "Only regular users are allowed to book tours", HttpStatus.FORBIDDEN),
    USER_DISABLED(1021, "Your account is not authorized to perform this action", HttpStatus.FORBIDDEN),
    SESSION_EXPIRED(1022, "The login session has expired", HttpStatus.FORBIDDEN),
    UNAUTHORIZED(1023, "Unauthorized to cancel this booking", HttpStatus.UNAUTHORIZED),
    ROLE_NOT_ALLOWED(1024, "Only admin or tour-company has permission to perform this function", HttpStatus.UNAUTHORIZED),
    ROLE_ALLOWED_ADMIN(1025, "Only tour-company has permission to perform this function", HttpStatus.UNAUTHORIZED),
    TOUR_NOT_FOUND(1201, "Tour not found", HttpStatus.NOT_FOUND),
    EXCEED_MAX_PEOPLE(1202, "The number of people exceeds the tour's capacity", HttpStatus.BAD_REQUEST),
    BELOW_MIN_PEOPLE(1203, "The number of participants is below the tour's minimum requirement", HttpStatus.BAD_REQUEST),
    INVALID_BOOKING_DATE(1204, "The tour start date must be at least 4 days from today", HttpStatus.BAD_REQUEST),
    INVALID_PEOPLE_COUNT(1205, "The number of people must be a non-negative integer", HttpStatus.BAD_REQUEST),
    TOUR_NOT_ACTIVE(1206, "The tour is currently inactive", HttpStatus.BAD_REQUEST),
    DUPLICATE_PROMOTION_CODE(1207, "A promotion with this code already exists in the system", HttpStatus.BAD_REQUEST),
    TOUR_COMPANY_UNAUTHORIZED_TOUR(1208, "Tour-company can only add tours from their own company into the promotion.", HttpStatus.UNAUTHORIZED),
    BOOKING_NOT_FOUND(1209, "Booking not found", HttpStatus.NOT_FOUND),
    INVALID_BOOKING_STATUS(1210, "Booking cannot be cancelled in its current state", HttpStatus.BAD_REQUEST),
    INVALID_PROMOTION_TIME(1211, "The start time cannot be after the end time", HttpStatus.BAD_REQUEST),
    INVALID_TIME(1212, "The time must be greater than the current time", HttpStatus.BAD_REQUEST ),
    TOUR_START_DATE_NOT_FOUND(1213, "The start date of the tour is not found", HttpStatus.NOT_FOUND),
    PROMOTION_NOT_FOUND(1214, "Promotion not found", HttpStatus.BAD_REQUEST),
    PROMOTION_FORBIDDEN(1215, "You do not have permission to edit this promotion", HttpStatus.FORBIDDEN),
    OPERATION_NOT_ALLOWED(1304, "Operation not allowed for this role", HttpStatus.FORBIDDEN),
    INVALID_REQUEST(1400, "Invalid request", HttpStatus.BAD_REQUEST),
    FEEDBACK_NOT_FOUND(1501, "No approved feedback found for this tour", HttpStatus.NOT_FOUND),
    INVALID_DATE_FORMAT(1601, "Invalid date format", HttpStatus.BAD_REQUEST),
    INVALID_FEEDBACK(1042, "Invalid feedback", HttpStatus.BAD_REQUEST),
    SERVICE_NOT_FOUND(1043,"Not found Service",HttpStatus.BAD_REQUEST),
    ACTIVITY_NOT_FOUND(1044,"Not found Activity",HttpStatus.BAD_REQUEST),
    TOUR_ALREADY_REPORTED(1602, "You have already reported this tour.", HttpStatus.BAD_REQUEST),
    USER_BLOCKED(1045,"Account is blocked",HttpStatus.BAD_REQUEST),
    ;



    // 1100–1199: Quản lý người dùng
    // 1200–1299: Quản lý tour
    // 1300–1399: Thanh toán




    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

}
