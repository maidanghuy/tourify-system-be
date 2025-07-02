package com.example.tourify_system_be.exception;

public class AppException extends RuntimeException {

    private ErrorCode errorCode;


    public AppException(ErrorCode errorCode, String s) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}