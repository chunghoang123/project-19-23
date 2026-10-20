package com.cinex.exception;

/** Yêu cầu không hợp lệ từ phía người dùng (ghế hết, mã ưu đãi sai...). */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
