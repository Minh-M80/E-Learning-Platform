package com.example.E_Learning_Platform.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    USER_EXISTED(1002,"User existed", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1003,"User not existed",HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1004,"Password wrong",HttpStatus.UNAUTHORIZED);

    ErrorCode(int code,String message,HttpStatusCode statusCode){
        this.code=code;
        this.message=message;
        this.statusCode=statusCode;
    }
    private int code;
    private String message;
    private HttpStatusCode statusCode;
}
