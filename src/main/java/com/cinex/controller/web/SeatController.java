package com.cinex.controller.web;

import com.cinex.domain.Showtime;
import com.cinex.dto.SeatMapView;
import com.cinex.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @GetMapping("/booking/seats/{showtimeId}")
    public String select(@PathVariable Integer showtimeId, Model model) {
        Showtime showtime = seatService.getShowtime(showtimeId);
        SeatMapView seatMap = seatService.buildSeatMap(showtimeId);

        model.addAttribute("showtime", showtime);
        model.addAttribute("seatMap", seatMap);
        return "booking/seats";
    }
}
