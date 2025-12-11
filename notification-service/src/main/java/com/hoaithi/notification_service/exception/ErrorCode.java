package com.hoaithi.notification_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    PROFILE_NOT_EXISTED(1011, "Profile not existed", HttpStatus.NOT_FOUND),
    USER_CHANNEL_SAME(1012,"User and Channel are the same", HttpStatus.BAD_REQUEST),
    SUBSCRIPTION_NOT_EXISTED(1011, "Subscription not existed", HttpStatus.NOT_FOUND),
    RESOURCE_NOT_FOUND(2001, "Resource not found", HttpStatus.NOT_FOUND),
    ALREADY_MEMBER(2002, "You already have an active membership", HttpStatus.BAD_REQUEST),
    IS_NOT_ACTIVE(2003, "This resource is not active", HttpStatus.BAD_REQUEST),
    DUPLICATE_RESOURCE(2004, "Resource already exists", HttpStatus.BAD_REQUEST),

    PAYMENT_FAILED(3001, "Payment processing failed", HttpStatus.BAD_REQUEST),
    FORBIDDEN(3002, "Action is forbidden", HttpStatus.FORBIDDEN),
    INVALID_REQUEST(3003, "Invalid request", HttpStatus.BAD_REQUEST),
    PAYMENT_CANCELED(3004, "Payment was canceled", HttpStatus.BAD_REQUEST),
    NOTIFICATION_NOT_FOUND(2222,"Notification not found" , HttpStatus.BAD_REQUEST);
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
