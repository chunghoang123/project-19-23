package com.cinex.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Sinh mã đơn hàng duy nhất cho booking, ticket, bill và payment.
 */
@Component
@RequiredArgsConstructor
public class CodeGenerator {

    private static final DateTimeFormatter CODE_TIME = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    private final Random random = new Random();

    /** Ví dụ {@code BK2610221250373074}. */
    public String generate(String prefix) {
        return prefix + LocalDateTime.now().format(CODE_TIME) + random.nextInt(1000, 9999);
    }
}
