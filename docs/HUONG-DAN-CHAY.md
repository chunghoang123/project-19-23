# Hướng dẫn chạy dự án CineX

> Tài liệu chi tiết cho người mới clone repo. Tóm tắt nhanh xem [README](../README.md).

## 0. Yêu cầu trước khi bắt đầu

| Thành phần | Bắt buộc | Ghi chú |
|---|---|---|
| JDK 17 | ✅ | Dự án target Java 17 (`build.gradle`) |
| MySQL 8 | ✅ | Hoặc dùng MySQL trong Docker |
| Docker Desktop | tuỳ chọn | Chỉ cần khi chạy bằng Docker Compose |
| Gradle | ❌ | Dùng wrapper `./gradlew` (tự tải) |

Kiểm tra nhanh:

```bash
java -version      # phải là 17
docker --version   # nếu dùng Docker
```

## 1. Chuẩn bị file cấu hình `.env`

> **Nếu bạn chỉ muốn chạy thử ở máy local** thì có thể **bỏ qua bước này**:
> `application.yml` đã có sẵn giá trị mặc định cho MySQL local
> (`DB_HOST=localhost`, `DB_NAME=cinex_local`, `DB_USER=cinex`, `DB_PASSWORD=cinex`),
> nên bấm **Run** trong IntelliJ hoặc `./gradlew bootRun` là chạy được ngay.
> File `.env` chỉ cần thiết khi dùng Docker Compose hoặc khi muốn bật tích hợp ngoài.

```bash
cp .env.example .env        # Linux/macOS
copy .env.example .env      # Windows PowerShell
```

Mở `.env` và điền các giá trị cần thiết:

```bash
# ----- Database (ghi đè giá trị mặc định khi chạy trực tiếp bằng Gradle) -----
DB_HOST=127.0.0.1
DB_PORT=3306
DB_NAME=cinex_local
DB_USER=cinex
DB_PASSWORD=cinex

# ----- Tích hợp bên ngoài -----
TMDB_API_KEY=...            # lấy tại https://www.themoviedb.org/settings/api
YOUTUBE_API_KEY=...         # Google Cloud Console -> YouTube Data API v3
PAYOS_CLIENT_ID=...         # https://my.payos.vn
PAYOS_API_KEY=...
PAYOS_CHECKSUM_KEY=...
```

> `.env` đã nằm trong `.gitignore` — **không** commit key thật lên Git.
> Kiểm tra ứng dụng đã nhận được key chưa: `GET /api/public/integrations`.
>
> **Thứ tự ưu tiên**: biến môi trường (`DB_*`) > giá trị mặc định trong `application.yml`.
> Docker Compose luôn truyền `DB_*` qua `environment:` nên production không bị ảnh hưởng.

## 2. Chọn cách chạy

### Cách A — Docker Compose (nhanh nhất, giống hệt production)

```bash
docker compose --env-file .env up -d --build
```

| Thành phần | Cổng |
|---|---|
| nginx (truy cập chính) | **8090** → http://localhost:8090 |
| app (Spring Boot) | 8080/tcp — chỉ nội bộ |
| mysql | 3306/tcp — chỉ nội bộ |

Lệnh thường dùng:

```bash
docker compose --env-file .env ps            # xem trạng thái
docker compose --env-file .env logs -f app  # xem log ứng dụng
docker compose --env-file .env up -d --build  # build lại & triển khai bản mới
docker compose --env-file .env restart app   # khởi động lại app
docker compose --env-file .env down          # dừng (giữ dữ liệu)
docker compose --env-file .env down -v       # dừng và XOÁ cả dữ liệu
```

### Cách B — Chạy trực tiếp bằng Gradle với MySQL sẵn có

**B1. Tạo database + user (chỉ làm 1 lần)**

```bash
mysql -u root -p -e "CREATE DATABASE cinex_local CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'cinex'@'localhost' IDENTIFIED BY 'cinex';
GRANT ALL PRIVILEGES ON cinex_local.* TO 'cinex'@'localhost';"
```

**B2. Nạp schema + 63 tỉnh/thành**

```bash
mysql -u root -p --default-character-set=utf8mb4 cinex_local < database/init.sql
```

**B3. Chạy ứng dụng**

```powershell
# Windows PowerShell
.\run-local.ps1                 # cổng 8080
.\run-local.ps1 -Port 8090      # đổi cổng
```

```bash
# Linux/macOS
export $(grep -v '^#' .env | xargs) && ./gradlew bootRun
```

Script `run-local.ps1` tự đọc toàn bộ biến trong `.env` (DB, TMDB, PayOS…) rồi chạy `gradlew bootRun`.

**B4. Hoặc chạy bằng IntelliJ IDEA**

Cách nhanh nhất: mở thư mục dự án, chờ Gradle sync xong rồi bấm **Run** trên
`CinexApplication` — app dùng sẵn giá trị mặc định cho MySQL local nên không cần cấu hình gì.

Repo có sẵn run configuration **CineX Local** tại `.run/CineX Local.run.xml`
(chọn trong thanh công cụ Run) dùng cho trường hợp muốn chạy song song 2 cổng
hoặc đổi cấu hình DB. Nếu IntelliJ báo thiếu module, vào *Run → Edit Configurations →
CineX Local* và chọn module `cinex.main`, sau đó kiểm tra tab *Environment variables* có đủ
`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`.

> **Lỗi thường gặp**: `Access denied for user 'cinex'@'localhost' (using password: NO)`
> → app không có `DB_PASSWORD`. Chạy bằng `run-local.ps1` (đọc `.env`) hoặc thêm các biến
> `DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD` vào *Environment variables* của run configuration.

**B5. Build jar chạy production**

```bash
./gradlew bootJar -x test      # ra build/libs/cinex.jar
java -jar build/libs/cinex.jar
```

## 3. Đăng nhập

| Vai trò | Email | Mật khẩu |
|---|---|---|
| Quản trị | `admin@cinex.vn` | `admin123` |
| Người dùng | `user@cinex.vn` | `user123` |

Đổi tài khoản admin qua `APP_SEED_ADMIN_EMAIL` / `APP_SEED_ADMIN_PASSWORD` trong `.env`.

## 4. Dữ liệu mẫu

Lần khởi động đầu, `DataSeeder` tự nạp:

- **45 chi nhánh rạp CineX** · **90 phòng chiếu** · **7.020 ghế**
- **14 phim** mẫu (poster/backdrop lấy từ TMDB) · **2.520 suất chiếu** cho 7 ngày tới
- **3 mã khuyến mãi**: `CINEX10` (giảm 10%, tối đa 50k, đơn ≥ 200k) · `STUDENT50` (giảm 50k, đơn ≥ 300k) · `VIP20` (giảm 20%, tối đa 100k, đơn ≥ 500k)

Tắt nạp dữ liệu mẫu: `APP_SEED_ENABLED=false` trong `.env`.

### Đồng bộ phim mới nhất từ TMDB

Vào **Quản trị → Phim** → nút *Đồng bộ từ TMDB*, chọn nguồn:

| Nguồn | Nội dung |
|---|---|
| Mới ra rạp | `/movie/now_playing` — phim vừa phát hành |
| Đang phổ biến | `/movie/popular` |
| Thị tranh tuần | `/trending/movie/week` |
| Sắp chiếu | `/movie/upcoming` |

Sau khi đồng bộ, hệ thống tự **phát hành 3 suất chiếu cho mỗi phim mới** vào các khung giờ còn trống
nên phim mới lập tức đặt vé được. Phim đã có `tmdb_id` sẽ được cập nhật, không tạo trùng.

## 5. Các tính năng chính

**Người dùng**: đăng ký/đăng nhập · xem phim & tìm kiếm · lịch chiếu theo ngày/rạp · chọn ghế trên sơ đồ thật ·
giữ ghế 15 phút · áp mã khuyến mãi · thanh toán tại quầy hoặc **online qua PayOS** · xem/huỷ vé · sửa hồ sơ.

**Quản trị** (`/admin`): bảng điều khiển doanh thu · quản lý phim (kèm đồng bộ TMDB) · rạp · đơn đặt vé · khuyến mãi.

**REST API**

| Method | Đường dẫn | Mô tả |
|---|---|---|
| GET | `/api/health` | Kiểm tra tình trạng ứng dụng |
| GET | `/api/public/integrations` | Các tích hợp đã cấu hình |
| GET | `/api/public/seat-map/{showtimeId}` | Sơ đồ ghế + ghế đã bán |
| POST | `/api/seat-holds` | Giữ ghế (cần đăng nhập) |
| DELETE | `/api/seat-holds/{code}` | Huỷ giữ ghế |
| POST | `/api/payments/payos/webhook` | Webhook xác nhận thanh toán PayOS |

## 6. Bảng biến môi trường

| Biến | Mặc định | Ý nghĩa |
|---|---|---|
| `APP_PORT` | `8080` | Cổng ứng dụng |
| `APP_NAME` | `CineX` | Tên hiển thị |
| `APP_SEED_ENABLED` | `true` | Nạp dữ liệu mẫu khi khởi động |
| `APP_SEED_ADMIN_EMAIL` / `APP_SEED_ADMIN_PASSWORD` | `admin@cinex.vn` / `admin123` | Tài khoản quản trị |
| `APP_SHOWTIME_DAYS_AHEAD` | `7` | Số ngày phát hành suất chiếu |
| `APP_HOLD_EXPIRE_MINUTES` | `15` | Số phút giữ ghế trước khi giải phóng |
| `APP_HOLD_EXPIRE_INTERVAL_MS` | `60000` | Chu kỳ quét đơn quá hạn (ms) |
| `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USER` / `DB_PASSWORD` | `localhost` / `3306` / `cinex` / `cinex` / rỗng | Kết nối MySQL |
| `MYSQL_DATABASE` / `MYSQL_USER` / `MYSQL_PASSWORD` / `MYSQL_ROOT_PASSWORD` | — | Dùng cho Docker Compose |
| `TMDB_API_KEY` / `TMDB_API_URL` / `TMDB_LANGUAGE` / `TMDB_REGION` | — / `https://api.themoviedb.org/3` / `vi-VN` / `VN` | Đồng bộ phim |
| `YOUTUBE_API_KEY` | — | Tìm trailer YouTube |
| `PAYOS_CLIENT_ID` / `PAYOS_API_KEY` / `PAYOS_CHECKSUM_KEY` | — | Thanh toán online |
| `PAYOS_BASE_URL` | `https://api-merchant.payos.vn` | API PayOS |
| `GOOGLE_CLIENT_ID` | — | Đăng nhập Google (chưa triển khai) |
| `FIREBASE_PROJECT_ID` / `FIREBASE_CLIENT_EMAIL` / `FIREBASE_PRIVATE_KEY` | — | Firebase (chưa triển khai) |
| `NVIDIA_API_KEY` / `NVIDIA_MODEL` | — / `meta/llama-3.1-8b-instruct` | Trợ lý AI (chưa triển khai) |

## 7. Xử lý sự cố

| Lỗi | Nguyên nhân & cách sửa |
|---|---|
| `Access denied for user 'cinex'@'localhost'` | Sai mật khẩu user, hoặc chưa `GRANT` đúng tên DB (`cinex_local.*`) |
| `Unknown database 'cinex_local'` | Chưa tạo database — làm lại B1 ở mục 2 |
| `Schema-validation: wrong column type` | `ddl-auto=validate` — schema trong DB không khớp entity. Nạp lại `database/init.sql` vào DB đúng, hoặc `ALTER` thêm cột thiếu (ví dụ `payments.checkout_url`) |
| `Port 8080 was already in use` | `netstat -ano \| findstr :8080` → `Stop-Process -Id <pid> -Force`, hoặc đổi `APP_PORT` |
| `Port 8090 đã dùng` (Docker) | `docker compose --env-file .env ps` xem container nào đang giữ cổng, dừng nó |
| `Cannot connect to the Docker daemon` | Mở app Docker Desktop, chờ icon báo "running" |
| Trang phim trắng, log báo `TemplateProcessingException` | Template lỗi cú pháp Thymeleaf — xem `docker compose logs app` hoặc `app.log` |
| Trailer không phát | YouTube chặn IP (429/CAPTCHA) — dùng `youtube-nocookie.com/embed` (đã cấu hình sẵn trong `trailer-modal.js`) |
| `gradlew` báo lỗi quyền trên Linux | `chmod +x gradlew` |

## 8. Triển khai lên server

```bash
cd "/home/chunghoang/project 19-23"
git pull --ff-only origin main
docker compose --env-file .env up -d --build
docker compose --env-file .env ps
```

Dữ liệu nằm trong volume `cinex_mysql_data` nên `down` không mất dữ liệu, chỉ `down -v` mới xoá.

## 9. Kiểm thử

```bash
./gradlew test        # unit test
./gradlew build       # build jar
./gradlew srs         # sinh lại tài liệu SRS + 5 sơ đồ trong docs/
```
