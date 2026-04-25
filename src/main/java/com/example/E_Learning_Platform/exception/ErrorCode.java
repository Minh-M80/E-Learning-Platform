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
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_ROLE(1008,"Role not valid",HttpStatus.BAD_REQUEST),
    INVALID_RESET_TOKEN(1009, "Reset token is invalid or expired", HttpStatus.BAD_REQUEST),
    LESSON_ORDER_DUPLICATED(1010,"Lesson duplicated",HttpStatus.BAD_REQUEST),
    LESSON_NOT_EXISTED(1011,"Lesson not found",HttpStatus.NOT_FOUND),
    COURSE_EXISTED(1012,"Course already exists in cart",HttpStatus.BAD_REQUEST),
    CART_ITEM_NOT_FOUND(1013, "Course không tồn tại trong giỏ hàng",HttpStatus.BAD_REQUEST),
    CANNOT_ADD_OWN_COURSE(1014,"Cannot add own course",HttpStatus.BAD_REQUEST),

    CART_NOT_FOUND(1015,"Cart not found",HttpStatus.NOT_FOUND),

    CART_EMPTY(1016,"Cart is empty",HttpStatus.BAD_REQUEST),
    ORDER_NOTOUND(1017, "Order not found", HttpStatus.NOT_FOUND),
    ENROLLMENT_EXISTED(1018, "User already enrolled this course", HttpStatus.BAD_REQUEST),
    ORDER_INVALID_STATUS(1019, "Order status is not valid for this action", HttpStatus.BAD_REQUEST),
    PAYMENT_INVALID_SIGNATURE(1020, "VNPay signature is invalid", HttpStatus.BAD_REQUEST),
    PAYMENT_INVALID_AMOUNT(1021, "Payment amount does not match order", HttpStatus.BAD_REQUEST),
    PAYMENT_CONFIG_INVALID(1022, "VNPay configuration is invalid", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_RESPONSE_INVALID(1023, "VNPay response data is invalid", HttpStatus.BAD_REQUEST);




    ErrorCode(int code,String message,HttpStatusCode statusCode){
        this.code=code;
        this.message=message;
        this.statusCode=statusCode;
    }
    private int code;
    private String message;
    private HttpStatusCode statusCode;
}
