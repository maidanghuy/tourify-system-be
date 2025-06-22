package com.example.tourify_system_be.exception;

import com.example.tourify_system_be.dto.request.APIResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.Objects;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String MIN_ATTRIBUTE = "min";

    /**
     * Bắt tất cả Exception chưa xác định
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse> handleException(Exception e) {
        APIResponse response = new APIResponse();
        response.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        response.setMessage(e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Bắt AppException có mã lỗi cụ thể
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<APIResponse> handleAppException(AppException e) {
        ErrorCode errorCode = e.getErrorCode();

        APIResponse response = new APIResponse();
        response.setCode(errorCode.getCode());
        response.setMessage(errorCode.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(response);
    }

    /**
     * Bắt lỗi validate @Valid trong @RequestBody (DTO)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();

        String message = (fieldError != null)
                ? fieldError.getDefaultMessage()
                : "Validation failed";

        APIResponse apiResponse = new APIResponse();
        apiResponse.setCode(ErrorCode.INVALID_KEY.getCode());
        apiResponse.setMessage(message);
        return ResponseEntity.badRequest().body(apiResponse);
    }

    /**
     * Bắt lỗi validate @RequestParam, @PathVariable (ví dụ @Min, @NotNull)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<APIResponse> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse("Invalid input");

        APIResponse response = new APIResponse();
        response.setCode(ErrorCode.INVALID_KEY.getCode());
        response.setMessage(message);

        return ResponseEntity.badRequest().body(response);
    }
}
