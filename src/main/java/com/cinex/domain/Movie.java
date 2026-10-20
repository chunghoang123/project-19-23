package com.cinex.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
public class Movie {

    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_ARCHIVED = "archived";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tmdb_id")
    private Integer tmdbId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "original_title", length = 200)
    private String originalTitle;

    @Column(length = 200)
    private String genre;

    @Column(nullable = false)
    private Integer duration = 0;

    @Column(nullable = false, length = 10)
    private String rating = "P";

    @Column(length = 500)
    private String poster;

    @Column(length = 500)
    private String backdrop;

    @Column(length = 200)
    private String director;

    @Column(name = "cast_members", columnDefinition = "TEXT")
    private String castMembers;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "ticket_price", nullable = false)
    private Integer ticketPrice = 0;

    @Column(name = "vip_price", nullable = false)
    private Integer vipPrice = 0;

    @Column(name = "trailer_url", length = 500)
    private String trailerUrl;

    @Column(nullable = false, length = 10)
    private String language = "vi";

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal popularity = BigDecimal.ZERO;

    @Column(nullable = false, length = 20)
    private String status = STATUS_ACTIVE;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /** Thể loại dạng danh sách: "Hành động, Tình cảm". */
    public String getGenreList() {
        return genre == null ? "" : genre;
    }

    public boolean isNowShowing() {
        return releaseDate != null && !releaseDate.isAfter(LocalDate.now());
    }
}
