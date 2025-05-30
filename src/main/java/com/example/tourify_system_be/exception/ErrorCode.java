package com.example.tourify_system_be.exception;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1003, "User not found", HttpStatus.NOT_FOUND),
    EMAIL_NOT_FOUND(1004, "Email not found", HttpStatus.NOT_FOUND),
    FAIL_TO_SEEN_EMAIL(1005, "Fail to seen email", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(1006, "Invalid token", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED(1007, "Token expired", HttpStatus.BAD_REQUEST),
    TOKEN_ALREADY_USED(1008, "Token already used", HttpStatus.BAD_REQUEST),
    PASSWORD_CONFIRMATION_MISMATCH(1009, "Password confirmation does not match", HttpStatus.BAD_REQUEST),
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
