package com.cinex.exception;

/** Không tìm thấy tài nguyên (bộ phim, rạp, suất chiếu...). */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException of(String what, Object id) {
        return new NotFoundException("Không tìm thấy " + what + ": " + id);
    }
}
