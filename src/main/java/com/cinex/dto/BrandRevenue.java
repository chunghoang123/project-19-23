package com.cinex.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BrandRevenue {

    private String brand;
    private long revenue;
    private long bookings;
}
