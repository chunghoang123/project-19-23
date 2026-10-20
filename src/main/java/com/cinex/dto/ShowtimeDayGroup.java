package com.cinex.dto;

import com.cinex.domain.Showtime;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * Nhóm suất chiếu theo ngày, phục vụ trang "Lịch chiếu".
 */
@Getter
@Builder
public class ShowtimeDayGroup {

    private LocalDate date;
    private List<Showtime> showtimes;

    /** Danh sách rạp có suất chiếu trong ngày (không trùng lặp, giữ thứ tự xuất hiện). */
    private List<Integer> cinemaIds;
}
