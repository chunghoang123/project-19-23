# CineX - Hệ thống đặt vé xem phim (Spring Boot)

Website đặt vé xem phim cho **một chuỗi rạp duy nhất** (thương hiệu CineX).
Người dùng xem phim, chọn rạp và suất chiếu, đặt ghế, thanh toán và quản lý vé.
Quản trị viên theo dõi phim, rạp, lịch chiếu, đơn đặt vé và khuyến mãi.

> **Trạng thái: KHUNG (skeleton).** Toàn bộ nghiệp vụ lõi đã chạy được.
> Các tích hợp bên ngoài (TMDB, YouTube, PayOS, NVIDIA AI, Google OAuth, Firebase)
> mới chỉ có chỗ cấu hình — điền khoá thật vào `.env` khi cần.

## Công nghệ

| Thành phần | Công nghệ |
|---|---|
| Backend | Spring Boot 3.5.16 · Java 17 · Spring Web MVC |
| Frontend | Thymeleaf 3.1 · Spring Security 6 · Bootstrap-style CSS thuần |
| Cơ sở dữ liệu | MySQL 8 (JPA/Hibernate 6) |
| Build | Gradle 8.14 (build trong Docker multi-stage) |
| Triển khai | Docker Compose: MySQL + Spring Boot + Nginx reverse proxy |
| Tích hợp | TMDB (phim), PayOS (thanh toán online) |

## Cấu trúc thư mục

```
project 19-23/
├── build.gradle / settings.gradle      # Gradle + Spring Boot 3.5.16
├── Dockerfile                          # build multi-stage (gradle:8.14-jdk17 → temurin:17-jre)
├── docker-compose.yml                  # mysql + app + nginx
├── .env.example                        # mẫu cấu hình (copy thành .env)
├── database/init.sql                   # schema + 63 tỉnh/thành
├── nginx/default.conf                  # reverse proxy
├── docs/                               # tài liệu SRS + 5 sơ đồ
│   ├── SrsGenerator.java               # bộ sinh tài liệu (100% Java thuần)
│   ├── SRS_Cinex_v1.0.md / .html / .docx
│   └── img/*.png                       # erd, usecase, flow, state, sitemap
└── src/main/
    ├── java/com/cinex/
    │   ├── CinexApplication.java
    │   ├── config/          # SecurityConfig, WebConfig, IntegrationProperties
    │   ├── controller/
    │   │   ├── api/         # REST API (health, seat map, seat-hold)
    │   │   └── web/         # controller render Thymeleaf
    │   ├── domain/          # 14 entity JPA
    │   ├── dto/             # record + form object
    │   ├── exception/       # ApiExceptionHandler, WebExceptionHandler
    │   ├── init/            # DataSeeder + dữ liệu mẫu (CinemaSeed, MovieSeed)
    │   ├── repository/      # Spring Data JPA (14 repository)
    │   ├── security/        # AppUserDetails, CinexUserDetailsService, CurrentUser
    │   └── service/         # nghiệp vụ
    └── resources/
        ├── application.yml
        ├── static/css|js
        └── templates/       # layout + các trang
```

## Chạy dự án

> 📖 **Hướng dẫn đầy đủ**: [`docs/HUONG-DAN-CHAY.md`](docs/HUONG-DAN-CHAY.md)

### Cách 1: Docker Compose (khuyến nghị)

```bash
cp .env.example .env        # sửa mật khẩu MySQL nếu cần
docker compose --env-file .env up -d --build
```

Truy cập: <http://localhost:8090> (đổi được qua `APP_PORT` trong `.env`)

### Cách 2: Chạy trực tiếp với MySQL sẵn có

```powershell
# 1. Tạo database + user (chạy 1 lần)
mysql -u root -p -e "CREATE DATABASE cinex_local CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'cinex'@'localhost' IDENTIFIED BY 'cinex';
GRANT ALL PRIVILEGES ON cinex_local.* TO 'cinex'@'localhost';"

# 2. Nạp schema + 63 tỉnh/thành
mysql -u root -p --default-character-set=utf8mb4 cinex_local < database/init.sql

# 3. Điền key vào .env rồi chạy (script tự nạp .env)
copy .env.example .env
.\run-local.ps1
```

Truy cập: <http://localhost:8080>

`run-local.ps1` đọc toàn bộ biến trong `.env` (TMDB, PayOS, cấu hình DB) rồi chạy
`gradlew bootRun`. Đổi cổng: `.\run-local.ps1 -Port 8090`.

### Tài khoản mẫu

| Vai trò | Email | Mật khẩu |
|---|---|---|
| Quản trị | `admin@cinex.vn` | `admin123` |
| Người dùng | `user@cinex.vn` | `user123` |

Đổi tài khoản quản trị qua `APP_SEED_ADMIN_EMAIL` / `APP_SEED_ADMIN_PASSWORD`.

## Dữ liệu mẫu

Khi khởi động lần đầu, `DataSeeder` nạp sẵn:

- **45 chi nhánh rạp CineX** — địa chỉ thực tế của các chi nhánh Lotte Cinema tại Việt Nam,
  đổi thương hiệu thành **CineX** (nằm trong `init/CinemaSeed.java`)
- 90 phòng chiếu (mỗi rạp 2 phòng: 2D 8×12 ghế, 3D 6×10 ghế) — 7.020 ghế
- 14 phim đang chiếu (`init/MovieSeed.java`)
- 2.520 suất chiếu cho 7 ngày tới
- 3 chương trình khuyến mãi: `CINEX10`, `STUDENT50`, `VIP20`

Tắt nạp dữ liệu mẫu: `APP_SEED_ENABLED=false`.
Nạp lại từ đầu: `docker compose down -v && docker compose --env-file .env up -d --build`.

## Tính năng đã có

**Người dùng**
- Đăng ký / đăng nhập (BCrypt + session), đổi mật khẩu, sửa hồ sơ
- Trang chủ: phim đang chiếu, sắp chiếu, lịch chiếu hôm nay, khuyến mãi, hệ thống rạp
- Danh sách phim có tìm kiếm và phân trang
- Lịch chiếu theo ngày / theo rạp
- Chọn ghế theo sơ đồ thật (ghế thường / VIP / đã bán), tự tính tổng tiền
- Giữ ghế 15 phút → thanh toán → sinh vé + hoá đơn, áp dụng mã khuyến mãi
- Danh sách vé của tôi, huỷ đơn (trả lại ghế)

**Quản trị** (`/admin`)
- Bảng điều khiển: số phim, rạp, suất chiếu, đơn đặt vé, doanh thu tuần, doanh thu theo thương hiệu
- Danh sách phim / rạp / đơn đặt vé / khuyến mãi

**REST API**

| Method | Đường dẫn | Mô tả |
|---|---|---|
| GET | `/api/health` | Kiểm tra tình trạng ứng dụng |
| GET | `/api/public/integrations` | Các tích hợp đã cấu hình (chỉ trả tên) |
| GET | `/api/public/seat-map/{showtimeId}` | Sơ đồ ghế + ghế đã bán |
| POST | `/api/seat-holds` | Giữ ghế (cần đăng nhập) |
| DELETE | `/api/seat-holds/{code}` | Hủy giữ ghế |

## Bổ sung tích hợp bên ngoài

Khung đã sẵn chỗ cấu hình tại `config/IntegrationProperties.java`. Chỉ cần điền
biến môi trường tương ứng trong `.env`:

| Tích hợp | Biến môi trường | Dùng để | Trạng thái |
|---|---|---|---|
| TMDB | `TMDB_API_KEY` | Đồng bộ danh sách phim, poster, trailer | ✅ đã triển khai |
| YouTube | `YOUTUBE_API_KEY` | Tìm trailer phim (YouTube Data API) | ✅ dùng kèm TMDB |
| PayOS | `PAYOS_CLIENT_ID`, `PAYOS_API_KEY`, `PAYOS_CHECKSUM_KEY` | Thanh toán online | ✅ đã triển khai |
| NVIDIA AI | `NVIDIA_API_KEY`, `NVIDIA_MODEL` | Trợ lý AI tư vấn bằng hội thoại | ⏳ chờ triển khai |
| Google OAuth | `GOOGLE_CLIENT_ID` | Đăng nhập bằng Google | ⏳ chờ triển khai |
| Firebase | `FIREBASE_PROJECT_ID`, `FIREBASE_CLIENT_EMAIL`, `FIREBASE_PRIVATE_KEY` | Push notification, đăng nhập | ⏳ chờ triển khai |

Kiểm tra trạng thái: `GET /api/public/integrations`.

### Đồng bộ phim từ TMDB

Trang **Quản trị → Phim** có nút *Đồng bộ từ TMDB*, chọn nguồn
(mới ra rạp / đang phổ biến / thị tranh tuần / sắp chiếu) và số trang cần lấy.
Mỗi phim được lấy chi tiết ở `/movie/{id}?append_to_response=videos,credits` nên có
thời lượng, thể loại, đạo diễn, diễn viên và trailer. Phim đã có `tmdb_id` sẽ được
cập nhật, không tạo trùng.

Sau khi đồng bộ, hệ thống tự **phát hành suất chiếu cho các phim mới** (3 suất/phim)
vào những khung giờ còn trống nên phim mới lập tức đặt vé được. Trang chủ ưu tiên
phim mới ra rạp: slider hero xoay 5 phim mới nhất, mục *Phim đề xuất* gộp
phim mới với phim được quan tâm nhiều.

### Thanh toán online PayOS

- Nút **Thanh toán online qua PayOS** xuất hiện ở trang chi tiết đơn khi còn trạng thái
  *chờ thanh toán* và đã cấu hình đủ 3 khoá.
- `POST /booking/{code}/payos` tạo link thanh toán, lưu `orderCode` (= id của bản ghi
  `payments`) và `checkout_url`.
- PayOS gọi lại webhook `POST /api/payments/payos/webhook`; server xác thực chữ ký
  HMAC bằng checksum key rồi mới xác nhận đơn (sinh vé, hoá đơn, cập nhật thanh toán).
- Webhook chưa đăng ký trên `my.payos.vn` thì chưa nhận được thông báo tự động;
  khi đó có thể dùng *Xác nhận thanh toán* tại quầy.

## Kiểm tra chất lượng

```bash
./gradlew test      # unit test
./gradlew build     # build jar
```

## Tài liệu SRS

Tài liệu đặc tả yêu cầu phần mềm nằm trong thư mục `docs/`, gồm 3 định dạng cùng nội dung:

| File | Dùng để |
|---|---|
| `docs/SRS_Cinex_v1.0.docx` | Nộp bài / in ra PDF |
| `docs/SRS_Cinex_v1.0.html` | Xem trên trình duyệt, in |
| `docs/SRS_Cinex_v1.0.md` | Bản nguồn để chỉnh sửa nhanh |

Nội dung gồm 7 phần: giới thiệu, yêu cầu tổng thể, **16 use case đã hiện thực + 6 use case mở rộng**,
danh mục thông báo/cảnh báo, yêu cầu phi chức năng, đối chiếu triển khai và so sánh với hệ thống Express/React.

**Toàn bộ tài liệu và 5 sơ đồ được sinh tự động bằng Java thuần** – không dùng Python, Node.js
hay LibreOffice:

```bash
./gradlew srs        # sinh lại docs/*.md, *.html, *.docx và docs/img/*.png
```

`docs/SrsGenerator.java` dùng `java.awt.Graphics2D` để vẽ ERD, Use Case, Luồng, Trạng thái và Site Map,
rồi đóng gói trực tiếp file DOCX (định dạng OOXML) kèm ảnh nhúng. Sửa nội dung trong file Java này
rồi chạy lại `./gradlew srs`.

## Lưu ý vận hành

- `spring.jpa.hibernate.ddl-auto=validate` — schema do `database/init.sql` quản lý,
  Hibernate chỉ kiểm tra khớp, không tự sửa bảng
- `spring.jpa.open-in-view=true` — cần cho Thymeleaf vì template truy cập quan hệ lazy
- Ràng buộc `UNIQUE (showtime_id, seat_id)` ở bảng `booking_seats` chống bán trùng ghế
- `.env` đã nằm trong `.gitignore`, không commit khoá thật lên Git
# project-19-23
