package com.cinex.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/** Số liệu tổng quan cho trang quản trị. */
@Getter
@Builder
public class AdminStats {

    private long totalMovies;
    private long activeMovies;
    private long totalCinemas;
    private long totalScreens;
    private long upcomingShowtimes;
    private long totalBookings;
    private long pendingBookings;
    private long revenueThisWeek;
    private long revenueLastWeek;

    /** Doanh thu 7 ngày gần nhất, mảng [t2..cn] để vẽ biểu đồ. */
    private List<Long> revenueDaily;

    /** Tổng doanh thu theo thương hiệu rạp (hiện chỉ có CineX). */
    private List<BrandRevenue> brandRevenue;
}
