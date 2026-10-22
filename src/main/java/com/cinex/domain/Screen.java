package com.cinex.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "screens")
@Getter
@Setter
@NoArgsConstructor
public class Screen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cinema_id", nullable = false)
    private Cinema cinema;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "screen_type", nullable = false, length = 30)
    private String screenType = "2D";

    @Column(name = "rows_count", nullable = false)
    private Integer rowsCount = 6;

    @Column(name = "cols_count", nullable = false)
    private Integer colsCount = 10;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
