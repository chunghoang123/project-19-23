package com.cinex.service;

import com.cinex.domain.BookingSeat;
import com.cinex.domain.Seat;
import com.cinex.domain.Showtime;
import com.cinex.dto.SeatMapView;
import com.cinex.exception.NotFoundException;
import com.cinex.repository.BookingSeatRepository;
import com.cinex.repository.SeatRepository;
import com.cinex.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final BookingSeatRepository bookingSeatRepository;

    @Transactional(readOnly = true)
    public Showtime getShowtime(Integer id) {
        return showtimeRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("suất chiếu", id));
    }

    /** Bố cục ghế của phòng chứa suất chiếu, kèm cờ ghế đã có người giữ/bán. */
    @Transactional(readOnly = true)
    public SeatMapView buildSeatMap(Integer showtimeId) {
        Showtime showtime = getShowtime(showtimeId);
        List<Seat> seats = seatRepository.findByScreenIdAndIsActiveTrueOrderByRowLabelAscSeatNumberAsc(
                showtime.getScreen().getId());
        List<Integer> taken = bookingSeatRepository.findTakenSeatIds(showtimeId);

        Map<String, List<SeatMapView.SeatItem>> rows = new LinkedHashMap<>();
        for (Seat seat : seats) {
            rows.computeIfAbsent(seat.getRowLabel(), key -> new ArrayList<>())
                    .add(SeatMapView.SeatItem.builder()
                            .id(seat.getId())
                            .label(seat.getLabel())
                            .type(seat.getSeatType())
                            .taken(taken.contains(seat.getId()))
                            .build());
        }

        List<SeatMapView.Row> rowViews = rows.entrySet().stream()
                .map(entry -> SeatMapView.Row.builder()
                        .label(entry.getKey())
                        .seats(entry.getValue())
                        .build())
                .toList();

        return SeatMapView.builder()
                .screenId(showtime.getScreen().getId())
                .screenName(showtime.getScreen().getName())
                .rowsCount(showtime.getScreen().getRowsCount())
                .colsCount(showtime.getScreen().getColsCount())
                .standardPrice(showtime.getBasePrice())
                .vipPrice(showtime.getVipPrice())
                .rows(rowViews)
                .takenSeatIds(taken)
                .build();
    }

    /** Giá của một ghế: ghế VIP dùng giá VIP, còn lại dùng giá thường. */
    @Transactional(readOnly = true)
    public int priceOf(Showtime showtime, Seat seat) {
        return Seat.TYPE_VIP.equalsIgnoreCase(seat.getSeatType())
                ? showtime.getVipPrice()
                : showtime.getBasePrice();
    }

    @Transactional(readOnly = true)
    public List<BookingSeat> findHeldSeats(Integer showtimeId) {
        return bookingSeatRepository.findByShowtimeIdAndStatusNot(showtimeId,
                BookingSeat.STATUS_RELEASED);
    }
}
