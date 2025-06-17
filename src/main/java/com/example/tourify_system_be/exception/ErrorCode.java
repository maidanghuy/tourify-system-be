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
    PLACE_NOT_FOUND(1015,"Place not found",HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_FOUND(1016,"Category not found",HttpStatus.BAD_REQUEST),
    NOT_SUBCOMPANY(1017,"Role isn't sub-company",HttpStatus.BAD_REQUEST),

    // 1100–1199: Quản lý người dùng
    // 1200–1299: Quản lý tour
    // 1300–1399: Thanh toán
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

}
