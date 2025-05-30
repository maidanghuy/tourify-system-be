package com.example.tourify_system_be.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    void handleRuntimeException(RuntimeException e){
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    void handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
    }
}
