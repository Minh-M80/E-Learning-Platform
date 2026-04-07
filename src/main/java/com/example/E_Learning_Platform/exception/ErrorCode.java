package com.example.E_Learning_Platform.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    USER_EXISTED(1002,"User existed", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1003,"User not existed",HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1004,"Password wrong",HttpStatus.UNAUTHORIZED),
    CATEGORY_NOT_EXISTED(1005,"Category not found",HttpStatus.NOT_FOUND),
    COURSE_NOT_EXISTED(1006,"Course not found",HttpStatus.NOT_FOUND),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN);

    ErrorCode(int code,String message,HttpStatusCode statusCode){
        this.code=code;
        this.message=message;
        this.statusCode=statusCode;
    }
    private int code;
    private String message;
    private HttpStatusCode statusCode;
}
