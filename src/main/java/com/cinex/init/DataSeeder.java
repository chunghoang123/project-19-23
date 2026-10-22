package com.cinex.init;

import com.cinex.domain.*;
import com.cinex.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Nạp dữ liệu mẫu khi ứng dụng khởi động lần đầu: vai trò, tài khoản quản trị,
 * 45 chi nhánh rạp CineX, phòng chiếu, ghế, phim, suất chiếu và khuyến mãi.
 *
 * <p>Các thao tác đều idempotent nên có thể chạy lại an toàn.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ProvinceRepository provinceRepository;
    private final CinemaRepository cinemaRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;
    private final PromotionRepository promotionRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.cinex.service.ShowtimeService showtimeService;

    @Value("${app.seed-enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed-admin-email:admin@cinex.vn}")
    private String adminEmail;

    @Value("${app.seed-admin-password:admin123}")
    private String adminPassword;

    @Value("${app.showtime-days-ahead:7}")
    private int showtimeDaysAhead;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedEnabled) {
            log.info("Bỏ qua nạp dữ liệu mẫu (app.seed-enabled=false)");
            return;
        }
        try {
            seedAll();
        } catch (Exception ex) {
            log.error("Không nạp được dữ liệu mẫu", ex);
        }
    }

    private void seedAll() {
        seedRoles();
        seedAdmin();
        seedDemoUser();
        seedCinemas();
        seedMovies();
        seedPromotions();
        seedShowtimes();
    }

    private void seedRoles() {
        createRoleIfMissing("admin", "Quản trị viên");
        createRoleIfMissing("user", "Người dùng");
        createRoleIfMissing("staff", "Nhân viên rạp");
    }

    private void createRoleIfMissing(String name, String description) {
        if (roleRepository.findByName(name).isEmpty()) {
            Role role = new Role();
            role.setName(name);
            role.setDescription(description);
            roleRepository.save(role);
        }
    }

    private void seedAdmin() {
        if (userRepository.existsByEmailIgnoreCase(adminEmail)) {
            return;
        }
        Role adminRole = roleRepository.findByName("admin").orElseThrow();
        User admin = new User();
        admin.setName("Quản trị CineX");
        admin.setEmail(adminEmail.toLowerCase());
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(adminRole);
        admin.setStatus("active");
        userRepository.save(admin);
        log.info("Đã tạo tài khoản quản trị: {}", adminEmail);
    }

    private void seedDemoUser() {
        if (userRepository.existsByEmailIgnoreCase("user@cinex.vn")) {
            return;
        }
        Role userRole = roleRepository.findByName("user").orElseThrow();
        User user = new User();
        user.setName("Nguyễn Văn A");
        user.setEmail("user@cinex.vn");
        user.setPassword(passwordEncoder.encode("user123"));
        user.setPhone("0900000001");
        user.setRole(userRole);
        user.setStatus("active");
        userRepository.save(user);
    }

    /** Tạo 45 chi nhánh CineX cùng phòng chiếu và ghế. */
    private void seedCinemas() {
        if (cinemaRepository.countByStatus("active") > 0) {
            log.info("Đã có dữ liệu rạp, bỏ qua ({})", cinemaRepository.count());
            return;
        }
        int created = 0;
        for (CinemaSeed.Entry entry : CinemaSeed.entries()) {
            Optional<Province> province = provinceRepository.findByNameIgnoreCase(entry.provinceName());

            Cinema cinema = new Cinema();
            cinema.setName(entry.name());
            cinema.setAddress(entry.address());
            cinema.setProvince(province.orElse(null));
            cinema.setCity(entry.provinceName());
            cinema.setRegion(province.map(Province::getRegion).orElse("Việt Nam"));
            cinema.setBrand("CineX");
            cinema.setStatus("active");
            cinema = cinemaRepository.save(cinema);

            createScreen(cinema, "Rạp 1", "2D", 8, 12);
            createScreen(cinema, "Rạp 2", "3D", 6, 10);
            created++;
        }
        log.info("Đã tạo {} chi nhánh rạp CineX", created);
    }

    private void createScreen(Cinema cinema, String name, String type, int rows, int cols) {
        Screen screen = new Screen();
        screen.setCinema(cinema);
        screen.setName(name);
        screen.setScreenType(type);
        screen.setRowsCount(rows);
        screen.setColsCount(cols);
        screen = screenRepository.save(screen);
        showtimeService.createSeatsFor(screen);
    }

    private void seedMovies() {
        if (movieRepository.count() > 0) {
            log.info("Đã có dữ liệu phim, bỏ qua ({})", movieRepository.count());
            return;
        }
        int created = 0;
        for (MovieSeed.Entry entry : MovieSeed.entries()) {
            Movie movie = new Movie();
            movie.setTitle(entry.title());
            movie.setGenre(entry.genre());
            movie.setDuration(entry.duration());
            movie.setRating(entry.rating());
            movie.setPoster(entry.poster());
            movie.setBackdrop(entry.backdrop());
            movie.setDirector(entry.director());
            movie.setDescription(entry.description());
            movie.setReleaseDate(entry.releaseDate());
            movie.setTicketPrice(entry.ticketPrice());
            movie.setVipPrice(entry.vipPrice());
            movie.setTrailerUrl(entry.trailerUrl());
            movie.setPopularity(entry.popularity());
            movie.setLanguage(entry.language());
            movie.setTmdbId(entry.tmdbId());
            movie.setStatus(Movie.STATUS_ACTIVE);
            movieRepository.save(movie);
            created++;
        }
        log.info("Đã tạo {} phim", created);
    }

    private void seedPromotions() {
        if (promotionRepository.count() > 0) {
            return;
        }
        Promotion welcome = new Promotion();
        welcome.setCode("CINEX10");
        welcome.setTitle("Giảm 10% cho khách mới");
        welcome.setDescription("Áp dụng cho đơn đặt vé đầu tiên từ 200.000đ");
        welcome.setDiscountType(Promotion.TYPE_PERCENT);
        welcome.setDiscountValue(10);
        welcome.setMaxDiscount(50000);
        welcome.setMinAmount(200000);
        welcome.setStartDate(LocalDate.now().minusDays(30));
        welcome.setEndDate(LocalDate.now().plusDays(90));
        welcome.setIsActive(Boolean.TRUE);
        promotionRepository.save(welcome);

        Promotion student = new Promotion();
        student.setCode("STUDENT50");
        student.setTitle("Ưu đãi sinh viên - giảm 50.000đ");
        student.setDescription("Giảm trực tiếp 50.000đ cho mọi hóa đơn từ 300.000đ");
        student.setDiscountType(Promotion.TYPE_FIXED);
        student.setDiscountValue(50000);
        student.setMinAmount(300000);
        student.setStartDate(LocalDate.now().minusDays(15));
        student.setEndDate(LocalDate.now().plusDays(60));
        student.setIsActive(Boolean.TRUE);
        promotionRepository.save(student);

        Promotion vip = new Promotion();
        vip.setCode("VIP20");
        vip.setTitle("Thành viên VIP - giảm 20%");
        vip.setDescription("Tối đa giảm 100.000đ mỗi đơn");
        vip.setDiscountType(Promotion.TYPE_PERCENT);
        vip.setDiscountValue(20);
        vip.setMaxDiscount(100000);
        vip.setMinAmount(500000);
        vip.setStartDate(LocalDate.now().minusDays(10));
        vip.setEndDate(LocalDate.now().plusDays(120));
        vip.setUsageLimit(1000);
        vip.setIsActive(Boolean.TRUE);
        promotionRepository.save(vip);
    }

    private void seedShowtimes() {
        if (showtimeRepository.count() > 0) {
            log.info("Đã có lịch chiếu, bỏ qua ({})", showtimeRepository.count());
            return;
        }
        int created = showtimeService.generateShowtimes(showtimeDaysAhead, 4);
        log.info("Đã tạo {} suất chiếu", created);
    }
}
