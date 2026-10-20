package com.cinex.controller.web;

import com.cinex.domain.Movie;
import com.cinex.service.MovieService;
import com.cinex.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;
    private final ShowtimeService showtimeService;

    @GetMapping("/movies")
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Page<Movie> movies = movieService.search(q, page, 12);
        model.addAttribute("movies", movies);
        model.addAttribute("keyword", q);
        model.addAttribute("nowShowing", movieService.findNowShowing());
        return "movies/list";
    }

    @GetMapping("/movies/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Movie movie = movieService.getById(id);
        model.addAttribute("movie", movie);
        model.addAttribute("showtimes", showtimeService.findByMovie(id));
        return "movies/detail";
    }
}
