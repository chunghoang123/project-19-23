package com.cinex.service;

import com.cinex.config.IntegrationProperties;
import com.cinex.domain.Movie;
import com.cinex.exception.BadRequestException;
import com.cinex.repository.MovieRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Đồng bộ phim từ TMDB (https://www.themoviedb.org).
 *
 * <p>Dùng API v3 với {@code api_key}. Mỗi phim được lấy chi tiết qua
 * {@code /movie/{id}?append_to_response=videos,credits} để có thời lượng,
 * thể loại, diễn viên, đạo diễn và trailer trong cùng một lần gọi.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TmdbService {

    private static final String IMAGE_BASE = "https://image.tmdb.org/t/p";
    private static final String TRAILER_BASE = "https://www.youtube.com/watch?v=";

    private final MovieRepository movieRepository;
    private final IntegrationProperties properties;

    /** Kết quả đồng bộ để hiển thị lên giao diện quản trị. */
    public record SyncResult(int added, int updated, int skipped, String source) {

        public String summary() {
            return "Đồng bộ từ TMDB (" + source + "): thêm " + added
                    + ", cập nhật " + updated + ", bỏ qua " + skipped;
        }
    }

    @Transactional
    public SyncResult sync(String source, int pages) {
        if (!properties.isTmdbReady()) {
            throw new BadRequestException("Chưa cấu hình TMDB_API_KEY");
        }
        String normalized = normalizeSource(source);
        int limit = Math.min(Math.max(pages, 1), 5);
        Map<Integer, String> genreNames = fetchGenreNames();

        int added = 0;
        int updated = 0;
        int skipped = 0;

        for (int page = 1; page <= limit; page++) {
            List<TmdbMovieDto> results = fetchList(normalized, page);
            if (results.isEmpty()) {
                break;
            }
            for (TmdbMovieDto dto : results) {
                TmdbDetailDto detail = fetchDetail(dto.id());
                if (detail == null) {
                    skipped++;
                    continue;
                }
                boolean isNew = applyMovie(dto, detail, genreNames);
                if (isNew) {
                    added++;
                } else {
                    updated++;
                }
            }
        }
        log.info("Đồng bộ TMDB xong: thêm {}, cập nhật {}", added, updated);
        return new SyncResult(added, updated, skipped, normalized);
    }

    /**
     * Ghi phim vào CSDL. Trả về {@code true} nếu thêm mới, {@code false} nếu cập nhật.
     */
    private boolean applyMovie(TmdbMovieDto dto, TmdbDetailDto detail, Map<Integer, String> genreNames) {
        Optional<Movie> existing = movieRepository.findByTmdbId(dto.id());
        Movie movie = existing.orElseGet(Movie::new);
        boolean isNew = existing.isEmpty();

        movie.setTmdbId(dto.id());
        movie.setTitle(dto.title() != null ? dto.title() : detail.title());
        movie.setOriginalTitle(dto.originalTitle());
        movie.setDescription(dto.overview() != null && !dto.overview().isBlank()
                ? dto.overview() : detail.overview());
        movie.setPoster(dto.posterPath() != null ? IMAGE_BASE + "/w500" + dto.posterPath() : null);
        movie.setBackdrop(dto.backdropPath() != null ? IMAGE_BASE + "/original" + dto.backdropPath() : null);
        movie.setReleaseDate(parseDate(dto.releaseDate()));
        movie.setDuration(detail.runtime() != null ? detail.runtime() : 0);
        movie.setGenre(joinGenres(detail.genres(), genreNames));
        movie.setDirector(findDirector(detail.credits()));
        movie.setCastMembers(joinCast(detail.credits()));
        movie.setTrailerUrl(findTrailer(detail.videos()));
        movie.setLanguage(dto.originalLanguage() != null ? dto.originalLanguage() : "vi");
        movie.setPopularity(toPopularity(dto.voteAverage()));
        movie.setRating(guessRating(movie.getGenre()));
        movie.setStatus(Movie.STATUS_ACTIVE);

        int basePrice = priceOf(movie.getGenre());
        movie.setTicketPrice(basePrice);
        movie.setVipPrice((int) Math.round(basePrice * 1.6));

        movieRepository.save(movie);
        return isNew;
    }

    private List<TmdbMovieDto> fetchList(String source, int page) {
        String path = switch (source) {
            case "trending" -> "/trending/movie/week";
            case "upcoming" -> "/movie/upcoming";
            case "now_playing" -> "/movie/now_playing";
            default -> "/movie/popular";
        };
        TmdbPageDto body = restClient().get()
                .uri(uriBuilder -> uriBuilder.path(path)
                        .queryParam("api_key", properties.getTmdbApiKey())
                        .queryParam("language", properties.getTmdbLanguage())
                        .queryParam("region", properties.getTmdbRegion())
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .body(TmdbPageDto.class);
        return body == null || body.results() == null ? List.of() : body.results();
    }

    private TmdbDetailDto fetchDetail(Integer tmdbId) {
        try {
            return restClient().get()
                    .uri(uriBuilder -> uriBuilder.path("/movie/{id}")
                            .queryParam("api_key", properties.getTmdbApiKey())
                            .queryParam("language", properties.getTmdbLanguage())
                            .queryParam("append_to_response", "videos,credits")
                            .build(tmdbId))
                    .retrieve()
                    .body(TmdbDetailDto.class);
        } catch (Exception ex) {
            log.warn("Không lấy được chi tiết phim TMDB {}: {}", tmdbId, ex.getMessage());
            return null;
        }
    }

    private Map<Integer, String> fetchGenreNames() {
        TmdbGenreListDto body = restClient().get()
                .uri(uriBuilder -> uriBuilder.path("/genre/movie/list")
                        .queryParam("api_key", properties.getTmdbApiKey())
                        .queryParam("language", properties.getTmdbLanguage())
                        .build())
                .retrieve()
                .body(TmdbGenreListDto.class);
        if (body == null || body.genres() == null) {
            return Map.of();
        }
        return body.genres().stream()
                .filter(genre -> genre.id() != null && genre.name() != null)
                .collect(java.util.stream.Collectors.toMap(TmdbGenreDto::id, TmdbGenreDto::name, (a, b) -> a));
    }

    private RestClient restClient() {
        return RestClient.builder().baseUrl(properties.getTmdbApiUrl()).build();
    }

    private String normalizeSource(String source) {
        if (source == null) {
            return "popular";
        }
        String value = source.trim().toLowerCase();
        return switch (value) {
            case "trending", "upcoming", "now_playing" -> value;
            default -> "popular";
        };
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private BigDecimal toPopularity(Double voteAverage) {
        if (voteAverage == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(voteAverage).multiply(BigDecimal.TEN)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String joinGenres(List<TmdbGenreDto> genres, Map<Integer, String> genreNames) {
        if (genres == null || genres.isEmpty()) {
            return "";
        }
        return genres.stream()
                .map(genre -> genreNames.getOrDefault(genre.id(), genre.name()))
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .limit(4)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    private String findDirector(TmdbCreditsDto credits) {
        if (credits == null || credits.crew() == null) {
            return null;
        }
        return credits.crew().stream()
                .filter(person -> "Director".equalsIgnoreCase(person.job()))
                .map(TmdbPersonDto::name)
                .findFirst()
                .orElse(null);
    }

    private String joinCast(TmdbCreditsDto credits) {
        if (credits == null || credits.cast() == null) {
            return null;
        }
        return credits.cast().stream()
                .map(TmdbPersonDto::name)
                .filter(name -> name != null && !name.isBlank())
                .limit(6)
                .reduce((a, b) -> a + ", " + b)
                .orElse(null);
    }

    private String findTrailer(TmdbVideosDto videos) {
        if (videos == null || videos.results() == null) {
            return null;
        }
        return videos.results().stream()
                .filter(video -> "YouTube".equalsIgnoreCase(video.site()))
                .filter(video -> "Trailer".equalsIgnoreCase(video.type()))
                .sorted((a, b) -> Boolean.compare(Boolean.TRUE.equals(b.official()),
                        Boolean.TRUE.equals(a.official())))
                .map(TmdbVideoDto::key)
                .filter(key -> key != null && !key.isBlank())
                .findFirst()
                .map(key -> TRAILER_BASE + key)
                .orElse(null);
    }

    /** Giá vé suy ra từ thể loại, quy đổi ra đồng. */
    private int priceOf(String genre) {
        if (genre == null) {
            return 75000;
        }
        String value = genre.toLowerCase();
        if (value.contains("hành động") || value.contains("khoa học viễn tưởng")) {
            return 90000;
        }
        if (value.contains("kinh dị")) {
            return 80000;
        }
        if (value.contains("hoạt hình") || value.contains("gia đình")) {
            return 65000;
        }
        if (value.contains("tình cảm") || value.contains("chính kịch")) {
            return 75000;
        }
        return 75000;
    }

    /** Phân loại tuổi suy ra từ thể loại, chỉ mang tính gợi ý. */
    private String guessRating(String genre) {
        if (genre == null) {
            return "P";
        }
        String value = genre.toLowerCase();
        if (value.contains("kinh dị") || value.contains("kinh dị")) {
            return "T18";
        }
        if (value.contains("hành động") || value.contains("phiêu lưu")) {
            return "T13";
        }
        return "P";
    }

    // ------------------------------------------------------------------ DTO TMDB

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TmdbPageDto(@JsonProperty("results") List<TmdbMovieDto> results,
                        @JsonProperty("total_pages") Integer totalPages) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TmdbMovieDto(
            @JsonProperty("id") Integer id,
            @JsonProperty("title") String title,
            @JsonProperty("original_title") String originalTitle,
            @JsonProperty("overview") String overview,
            @JsonProperty("poster_path") String posterPath,
            @JsonProperty("backdrop_path") String backdropPath,
            @JsonProperty("release_date") String releaseDate,
            @JsonProperty("vote_average") Double voteAverage,
            @JsonProperty("original_language") String originalLanguage) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TmdbDetailDto(
            @JsonProperty("id") Integer id,
            @JsonProperty("title") String title,
            @JsonProperty("overview") String overview,
            @JsonProperty("runtime") Integer runtime,
            @JsonProperty("genres") List<TmdbGenreDto> genres,
            @JsonProperty("credits") TmdbCreditsDto credits,
            @JsonProperty("videos") TmdbVideosDto videos) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TmdbGenreDto(@JsonProperty("id") Integer id, @JsonProperty("name") String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TmdbGenreListDto(@JsonProperty("genres") List<TmdbGenreDto> genres) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TmdbCreditsDto(
            @JsonProperty("cast") List<TmdbPersonDto> cast,
            @JsonProperty("crew") List<TmdbPersonDto> crew) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TmdbPersonDto(
            @JsonProperty("name") String name,
            @JsonProperty("job") String job) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TmdbVideosDto(@JsonProperty("results") List<TmdbVideoDto> results) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TmdbVideoDto(
            @JsonProperty("key") String key,
            @JsonProperty("site") String site,
            @JsonProperty("type") String type,
            @JsonProperty("official") Boolean official) {
    }
}
