package com.cinex.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Bố cục ghế của một suất chiếu kèm trạng thái đã bán.
 */
@Getter
@Builder
public class SeatMapView {

    private Integer screenId;
    private String screenName;
    private Integer rowsCount;
    private Integer colsCount;
    private Integer standardPrice;
    private Integer vipPrice;

    /** Mỗi hàng: nhãn hàng + danh sách ghế. */
    private List<Row> rows;

    /** Ghế đã bán/đang giữ. */
    private List<Integer> takenSeatIds;

    @Getter
    @Builder
    public static class Row {
        private String label;
        private List<SeatItem> seats;
    }

    @Getter
    @Builder
    public static class SeatItem {
        private Integer id;
        private String label;
        private String type;
        private boolean taken;
    }
}
