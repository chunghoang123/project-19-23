-- =============================================================================
--  database/init.sql - Schema cho CineX (Spring Boot)
--  Một chuỗi rạp duy nhất (thương hiệu CineX), dùng lại địa chỉ các chi nhánh
--  Lotte Cinema tại Việt Nam nhưng đổi tên thương hiệu thành CineX.
--  MySQL chỉ chạy file này ở LẦN KHỞI ĐỘNG ĐẦU TIÊN (volume còn trống).
-- =============================================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------------ ROLES
CREATE TABLE IF NOT EXISTS roles (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(255) DEFAULT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------ USERS
CREATE TABLE IF NOT EXISTS users (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(100) DEFAULT NULL,
    password   VARCHAR(255) DEFAULT NULL,
    phone      VARCHAR(20)  DEFAULT NULL,
    avatar     VARCHAR(255) DEFAULT NULL,
    role_id    INT NOT NULL DEFAULT 2,
    status     VARCHAR(20) NOT NULL DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_users_email (email),
    UNIQUE INDEX idx_users_phone (phone),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------- PROVINCES
CREATE TABLE IF NOT EXISTS provinces (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    code      VARCHAR(20)  NOT NULL UNIQUE,
    name      VARCHAR(100) NOT NULL,
    region    VARCHAR(50)  NOT NULL,
    latitude  DECIMAL(10,7) NOT NULL,
    longitude DECIMAL(10,7) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------- CINEMAS
CREATE TABLE IF NOT EXISTS cinemas (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    province_id INT DEFAULT NULL,
    name        VARCHAR(200) NOT NULL,
    address     VARCHAR(500) NOT NULL,
    phone       VARCHAR(30)  DEFAULT NULL,
    lat         DECIMAL(10,8) DEFAULT NULL,
    lng         DECIMAL(11,8) DEFAULT NULL,
    district    VARCHAR(100) DEFAULT NULL,
    city        VARCHAR(100) DEFAULT NULL,
    region      VARCHAR(50)  DEFAULT NULL,
    brand       VARCHAR(50)  NOT NULL DEFAULT 'CineX',
    status      VARCHAR(20)  NOT NULL DEFAULT 'active',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_cinemas_city (city),
    CONSTRAINT fk_cinemas_province FOREIGN KEY (province_id) REFERENCES provinces (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------- SCREENS
CREATE TABLE IF NOT EXISTS screens (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    cinema_id   INT NOT NULL,
    name        VARCHAR(100) NOT NULL,
    screen_type VARCHAR(30)  NOT NULL DEFAULT '2D',
    rows_count  INT NOT NULL DEFAULT 6,
    cols_count  INT NOT NULL DEFAULT 10,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_screens_cinema_name (cinema_id, name),
    CONSTRAINT fk_screens_cinema FOREIGN KEY (cinema_id) REFERENCES cinemas (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------ SEATS
-- Bố cục ghế cố định theo phòng, không nhân bản theo suất chiếu
CREATE TABLE IF NOT EXISTS seats (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    screen_id INT NOT NULL,
    row_label VARCHAR(5) NOT NULL,
    seat_number INT NOT NULL,
    seat_type VARCHAR(20) NOT NULL DEFAULT 'standard',
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    UNIQUE INDEX idx_seats_screen_row_number (screen_id, row_label, seat_number),
    CONSTRAINT fk_seats_screen FOREIGN KEY (screen_id) REFERENCES screens (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------- MOVIES
CREATE TABLE IF NOT EXISTS movies (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    tmdb_id       INT DEFAULT NULL,
    title         VARCHAR(200) NOT NULL,
    original_title VARCHAR(200) DEFAULT NULL,
    genre         VARCHAR(200) DEFAULT NULL,
    duration      INT NOT NULL DEFAULT 0,
    rating        VARCHAR(10)  NOT NULL DEFAULT 'P',
    poster        VARCHAR(500) DEFAULT NULL,
    backdrop      VARCHAR(500) DEFAULT NULL,
    director      VARCHAR(200) DEFAULT NULL,
    cast_members  TEXT DEFAULT NULL,
    description   TEXT DEFAULT NULL,
    release_date  DATE DEFAULT NULL,
    ticket_price  INT NOT NULL DEFAULT 0,
    vip_price     INT NOT NULL DEFAULT 0,
    trailer_url   VARCHAR(500) DEFAULT NULL,
    language      VARCHAR(10) NOT NULL DEFAULT 'vi',
    popularity    DECIMAL(10,2) NOT NULL DEFAULT 0,
    status        VARCHAR(20) NOT NULL DEFAULT 'active',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_movies_tmdb (tmdb_id),
    INDEX idx_movies_popularity (popularity),
    INDEX idx_movies_release_date (release_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------- SHOWTIMES
CREATE TABLE IF NOT EXISTS showtimes (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    movie_id   INT NOT NULL,
    cinema_id  INT NOT NULL,
    screen_id  INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time   DATETIME NOT NULL,
    base_price INT NOT NULL DEFAULT 0,
    vip_price  INT NOT NULL DEFAULT 0,
    status     VARCHAR(20) NOT NULL DEFAULT 'scheduled',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_showtimes_movie (movie_id),
    INDEX idx_showtimes_cinema_start (cinema_id, start_time),
    INDEX idx_showtimes_start (start_time),
    CONSTRAINT fk_showtimes_movie  FOREIGN KEY (movie_id)  REFERENCES movies (id)  ON DELETE CASCADE,
    CONSTRAINT fk_showtimes_cinema FOREIGN KEY (cinema_id) REFERENCES cinemas (id) ON DELETE CASCADE,
    CONSTRAINT fk_showtimes_screen FOREIGN KEY (screen_id) REFERENCES screens (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------- BOOKINGS
CREATE TABLE IF NOT EXISTS bookings (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    code             VARCHAR(20) NOT NULL UNIQUE,
    user_id          INT NOT NULL,
    showtime_id      INT NOT NULL,
    total_amount     INT NOT NULL DEFAULT 0,
    discount_amount  INT NOT NULL DEFAULT 0,
    final_amount     INT NOT NULL DEFAULT 0,
    promotion_code   VARCHAR(50) DEFAULT NULL,
    payment_method   VARCHAR(50) NOT NULL DEFAULT 'cash',
    status           VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_bookings_user (user_id),
    INDEX idx_bookings_showtime (showtime_id),
    CONSTRAINT fk_bookings_user     FOREIGN KEY (user_id)     REFERENCES users (id),
    CONSTRAINT fk_bookings_showtime FOREIGN KEY (showtime_id) REFERENCES showtimes (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------- BOOKING_SEATS
CREATE TABLE IF NOT EXISTS booking_seats (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    booking_id  INT NOT NULL,
    showtime_id INT NOT NULL,
    seat_id     INT NOT NULL,
    seat_label  VARCHAR(10) NOT NULL,
    price       INT NOT NULL DEFAULT 0,
    status      VARCHAR(20) NOT NULL DEFAULT 'reserved',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_booking_seats_showtime (showtime_id),
    INDEX idx_booking_seats_booking (booking_id),
    CONSTRAINT fk_booking_seats_booking  FOREIGN KEY (booking_id)  REFERENCES bookings (id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_seats_showtime FOREIGN KEY (showtime_id) REFERENCES showtimes (id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_seats_seat     FOREIGN KEY (seat_id)     REFERENCES seats (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------- TICKETS
CREATE TABLE IF NOT EXISTS tickets (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    ticket_code VARCHAR(30) NOT NULL UNIQUE,
    booking_id  INT NOT NULL,
    user_id     INT NOT NULL,
    showtime_id INT NOT NULL,
    seat_label  VARCHAR(10) NOT NULL,
    qr_code     VARCHAR(120) DEFAULT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'issued',
    used_at     TIMESTAMP NULL DEFAULT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tickets_booking (booking_id),
    INDEX idx_tickets_user (user_id),
    CONSTRAINT fk_tickets_booking  FOREIGN KEY (booking_id)  REFERENCES bookings (id) ON DELETE CASCADE,
    CONSTRAINT fk_tickets_user     FOREIGN KEY (user_id)     REFERENCES users (id),
    CONSTRAINT fk_tickets_showtime FOREIGN KEY (showtime_id) REFERENCES showtimes (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------- PROMOTIONS
CREATE TABLE IF NOT EXISTS promotions (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    code           VARCHAR(50)  NOT NULL UNIQUE,
    title          VARCHAR(200) NOT NULL,
    description    TEXT DEFAULT NULL,
    discount_type  VARCHAR(20)  NOT NULL DEFAULT 'percent',
    discount_value INT NOT NULL DEFAULT 0,
    max_discount   INT DEFAULT NULL,
    min_amount     INT NOT NULL DEFAULT 0,
    start_date     DATE DEFAULT NULL,
    end_date       DATE DEFAULT NULL,
    usage_limit    INT DEFAULT NULL,
    used_count     INT NOT NULL DEFAULT 0,
    is_active      TINYINT(1) NOT NULL DEFAULT 1,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------ BILLS
CREATE TABLE IF NOT EXISTS bills (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(30) NOT NULL UNIQUE,
    booking_id  INT NOT NULL,
    user_id     INT NOT NULL,
    subtotal    INT NOT NULL DEFAULT 0,
    discount    INT NOT NULL DEFAULT 0,
    total       INT NOT NULL DEFAULT 0,
    issued_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_bills_user (user_id),
    CONSTRAINT fk_bills_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE,
    CONSTRAINT fk_bills_user    FOREIGN KEY (user_id)    REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------- PAYMENTS
CREATE TABLE IF NOT EXISTS payments (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    code              VARCHAR(30) NOT NULL UNIQUE,
    booking_id        INT NOT NULL,
    user_id           INT NOT NULL,
    provider          VARCHAR(30) NOT NULL DEFAULT 'cash',
    amount            INT NOT NULL DEFAULT 0,
    status            VARCHAR(20) NOT NULL DEFAULT 'pending',
    provider_order_id VARCHAR(80) DEFAULT NULL,
    provider_ref      VARCHAR(120) DEFAULT NULL,
    checkout_url      VARCHAR(500) DEFAULT NULL,
    paid_at           TIMESTAMP NULL DEFAULT NULL,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_payments_booking (booking_id),
    INDEX idx_payments_user (user_id),
    CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE,
    CONSTRAINT fk_payments_user    FOREIGN KEY (user_id)    REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================
-- SEED DATA CƠ BẢN
-- =====================
INSERT INTO roles (id, name, description) VALUES
    (1, 'admin', 'Quản trị viên'),
    (2, 'user',  'Người dùng'),
    (3, 'staff', 'Nhân viên rạp')
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO provinces (code, name, region, latitude, longitude) VALUES
('HN', 'Hà Nội', 'Miền Bắc', 21.0285, 105.8542),
('HCM', 'TP. Hồ Chí Minh', 'Miền Nam', 10.7769, 106.7009),
('HP', 'Hải Phòng', 'Miền Bắc', 20.8449, 106.6881),
('DN', 'Đà Nẵng', 'Miền Trung', 16.0471, 108.2068),
('CT', 'Cần Thơ', 'Miền Nam', 10.0452, 105.7469),
('AG', 'An Giang', 'Miền Nam', 10.5216, 105.1259),
('BRVT', 'Bà Rịa - Vũng Tàu', 'Miền Nam', 10.5417, 107.2429),
('BG', 'Bắc Giang', 'Miền Bắc', 21.2731, 106.1946),
('BK', 'Bắc Kạn', 'Miền Bắc', 22.147, 105.8348),
('BL', 'Bạc Liêu', 'Miền Nam', 9.294, 105.7216),
('BN', 'Bắc Ninh', 'Miền Bắc', 21.1861, 106.0763),
('BTE', 'Bến Tre', 'Miền Nam', 10.2433, 106.3756),
('BDI', 'Bình Định', 'Miền Trung', 13.782, 109.219),
('BD', 'Bình Dương', 'Miền Nam', 10.9804, 106.6519),
('BP', 'Bình Phước', 'Miền Nam', 11.7512, 106.7235),
('BTH', 'Bình Thuận', 'Miền Trung', 10.9333, 108.1),
('CM', 'Cà Mau', 'Miền Nam', 9.1769, 105.1524),
('CB', 'Cao Bằng', 'Miền Bắc', 22.6667, 106.25),
('DL', 'Đắk Lắk', 'Tây Nguyên', 12.6667, 108.05),
('DNO', 'Đắk Nông', 'Tây Nguyên', 12.2646, 107.6098),
('DB', 'Điện Biên', 'Miền Bắc', 21.386, 103.023),
('DNA', 'Đồng Nai', 'Miền Nam', 10.9574, 106.8427),
('DT', 'Đồng Tháp', 'Miền Nam', 10.4938, 105.6882),
('GL', 'Gia Lai', 'Tây Nguyên', 13.9833, 108.0),
('HG', 'Hà Giang', 'Miền Bắc', 22.8233, 104.9836),
('HNA', 'Hà Nam', 'Miền Bắc', 20.5835, 105.9229),
('HT', 'Hà Tĩnh', 'Miền Trung', 18.3559, 105.8877),
('HD', 'Hải Dương', 'Miền Bắc', 20.9373, 106.3145),
('HGI', 'Hậu Giang', 'Miền Nam', 9.7845, 105.4701),
('HB', 'Hòa Bình', 'Miền Bắc', 20.8172, 105.3376),
('HY', 'Hưng Yên', 'Miền Bắc', 20.6464, 106.0511),
('KH', 'Khánh Hòa', 'Miền Trung', 12.2388, 109.1967),
('KG', 'Kiên Giang', 'Miền Nam', 10.0125, 105.0809),
('KT', 'Kon Tum', 'Tây Nguyên', 14.3497, 108.0),
('LC', 'Lai Châu', 'Miền Bắc', 22.3964, 103.4582),
('LD', 'Lâm Đồng', 'Tây Nguyên', 11.9404, 108.4583),
('LS', 'Lạng Sơn', 'Miền Bắc', 21.8537, 106.7615),
('LCA', 'Lào Cai', 'Miền Bắc', 22.4809, 103.9755),
('LA', 'Long An', 'Miền Nam', 10.6956, 106.2431),
('ND', 'Nam Định', 'Miền Bắc', 20.4388, 106.1621),
('NA', 'Nghệ An', 'Miền Trung', 18.6796, 105.6813),
('NB', 'Ninh Bình', 'Miền Bắc', 20.2506, 105.9745),
('NT', 'Ninh Thuận', 'Miền Trung', 11.5643, 108.9886),
('PT', 'Phú Thọ', 'Miền Bắc', 21.3227, 105.402),
('PY', 'Phú Yên', 'Miền Trung', 13.0882, 109.0929),
('QB', 'Quảng Bình', 'Miền Trung', 17.4688, 106.6223),
('QNA', 'Quảng Nam', 'Miền Trung', 15.5394, 108.0191),
('QNG', 'Quảng Ngãi', 'Miền Trung', 15.1205, 108.7923),
('QNI', 'Quảng Ninh', 'Miền Bắc', 20.9712, 107.0448),
('QT', 'Quảng Trị', 'Miền Trung', 16.7403, 107.1855),
('ST', 'Sóc Trăng', 'Miền Nam', 9.6025, 105.9739),
('SL', 'Sơn La', 'Miền Bắc', 21.327, 103.9141),
('TN', 'Tây Ninh', 'Miền Nam', 11.3352, 106.1099),
('TB', 'Thái Bình', 'Miền Bắc', 20.4463, 106.3366),
('TNG', 'Thái Nguyên', 'Miền Bắc', 21.5672, 105.8252),
('TH', 'Thanh Hóa', 'Miền Trung', 19.8075, 105.7764),
('TTH', 'Thừa Thiên Huế', 'Miền Trung', 16.4637, 107.5909),
('TG', 'Tiền Giang', 'Miền Nam', 10.4493, 106.3421),
('TV', 'Trà Vinh', 'Miền Nam', 9.9347, 106.3453),
('TQ', 'Tuyên Quang', 'Miền Bắc', 21.8233, 105.2181),
('VL', 'Vĩnh Long', 'Miền Nam', 10.2537, 105.9722),
('VP', 'Vĩnh Phúc', 'Miền Bắc', 21.3089, 105.6049),
('YB', 'Yên Bái', 'Miền Bắc', 21.7051, 104.875)
ON DUPLICATE KEY UPDATE name = VALUES(name);
