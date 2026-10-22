package com.cinex.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** Biểu mẫu đặt vé: chọn ghế, khuyến mãi và phương thức thanh toán. */
@Getter
@Setter
public class CheckoutForm {

    @NotNull(message = "Thiếu suất chiếu")
    private Integer showtimeId;

    @NotEmpty(message = "Vui lòng chọn ít nhất một ghế")
    private List<Integer> seatIds = new ArrayList<>();

    private String promotionCode;

    private String paymentMethod = "cash";
}
