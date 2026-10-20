package com.cinex.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cinemas")
@Getter
@Setter
@NoArgsConstructor
public class Cinema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id")
    private Province province;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(length = 30)
    private String phone;

    @Column(precision = 10, scale = 8)
    private BigDecimal lat;

    @Column(precision = 11, scale = 8)
    private BigDecimal lng;

    @Column(length = 100)
    private String district;

    @Column(length = 100)
    private String city;

    @Column(length = 50)
    private String region;

    @Column(nullable = false, length = 50)
    private String brand = "CineX";

    @Column(nullable = false, length = 20)
    private String status = "active";

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
