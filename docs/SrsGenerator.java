/*
 * SrsGenerator.java - Bộ sinh tài liệu SRS cho dự án CineX (Spring Boot).
 *
 * Toàn bộ quy trình sinh tài liệu chạy bằng Java thuần: không dùng Python,
 * không dùng Node.js, không dùng LibreOffice, không thư viện ngoài.
 *  - Nội dung   -> mô hình khối (Block) dùng chung cho cả 3 định dạng
 *  - Markdown   -> SRS_Cinex_v1.0.md
 *  - HTML       -> SRS_Cinex_v1.0.html
 *  - DOCX       -> SRS_Cinex_v1.0.docx (đóng gói OOXML trực tiếp, nhúng PNG)
 *  - Sơ đồ      -> img/*.png vẽ bằng java.awt.Graphics2D
 *
 * Cách chạy:
 *   java docs/SrsGenerator.java [thư-mục-đầu-ra]
 */

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SrsGenerator {

    // ============================================================ THÔNG TIN
    static final String DOC_TITLE = "TÀI LIỆU ĐẶC TẢ YÊU CẦU PHẦN MỀM (SRS)";
    static final String DOC_SUBTITLE = "HỆ THỐNG ĐẶT VÉ XEM PHIM CINEX";
    static final String DOC_SUBTITLE2 = "(CineX Cinema Booking System - Spring Boot)";
    static final String DOC_DATE = "20/10/2026";

    static final String[][] META = {
            {"Dự án", "Hệ thống đặt vé xem phim CineX (bản Spring Boot)"},
            {"Đơn vị", "Học viện Công nghệ Bưu chính Viễn thông – Khoa Công nghệ Thông tin"},
            {"Người thực hiện", "Hoàng Văn Chung – D24TX"},
            {"Ngày cập nhật", DOC_DATE},
            {"Mã tài liệu", "SRS-CINEX-SB-2026-001"},
            {"Phiên bản", "1.0"},
            {"Trạng thái", "Bản đặc tả hiện trạng triển khai; các tính năng đang phát triển được ghi rõ riêng"},
    };

    static final String[][] VERSIONS = {
            {"1.0", DOC_DATE, "Hoàng Văn Chung",
             "Rà soát theo mã nguồn: cập nhật trạng thái tích hợp, phạm vi quản trị và vòng đời giữ ghế"},
    };

    static final String[][] REFS = {
            {DOC_DATE, "SRS_DatVePhimAI_v2.2_CapNhatTheoDuAn.docx", "Nội bộ – bản SRS của hệ thống Express/React"},
            {DOC_DATE, "Mã nguồn dự án project 19-23 (Spring Boot)", "Nội bộ"},
            {"—", "Tài liệu Spring Boot 3.5 Reference Documentation", "docs.spring.io"},
            {"—", "Tài liệu Thymeleaf + Spring Security 6", "thymeleaf.org / spring.io"},
    };

    // ============================================================== PHẦN 1
    static final String[][] P1_TEXT = {
            {"1.1 Mục đích tài liệu",
             "Tài liệu này đặc tả chi tiết các yêu cầu chức năng và phi chức năng của “Hệ thống Đặt vé "
             + "Xem phim CineX” bản triển khai bằng Spring Boot. Tài liệu làm cơ sở để hiểu đúng nghiệp vụ, "
             + "thiết kế giao diện và cơ sở dữ liệu, cài đặt hệ thống, đồng thời là căn cứ để kiểm thử và "
             + "nghiệm thu sản phẩm.",
             "Tài liệu được đối chiếu trực tiếp với mã nguồn hiện có trong thư mục `project 19-23`, nhằm "
             + "bảo đảm mọi hạng mục ghi trong đặc tả đều có dấu vết cài đặt hoặc được đánh dấu rõ ràng là "
             + "hạng mục mở rộng."},
            {"1.2 Phạm vi tài liệu",
             "Tài liệu bao gồm: sơ đồ ERD, đặc tả use case, sơ đồ luồng, sơ đồ chuyển trạng thái đơn đặt vé, "
             + "phân quyền, site map, danh mục thông báo/cảnh báo, yêu cầu phi chức năng và bảng đối chiếu triển khai.",
             "**Phạm vi nghiệp vụ:** hệ thống phục vụ **một chuỗi rạp duy nhất** (thương hiệu CineX). Không "
             + "liên kết, đối soát hay hiển thị dữ liệu của các chuỗi rạp khác; toàn bộ rạp trong hệ thống "
             + "cùng thuộc một thương hiệu và dùng chung hệ thống lịch chiếu, giá vé và khuyến mãi.",
             "**Hệ thống là website đặt vé trực tuyến**, không phải hệ thống quản lý vận hành rạp, không xử lý "
             + "bán vé tại quầy và không xử lý quét vé (check-in) trong phạm vi hiện hành."},
            {"1.3 Tổng quan ứng dụng",
             "Người dùng truy cập website để xem phim đang chiếu và sắp chiếu, tra cứu lịch chiếu theo ngày "
             + "hoặc theo rạp, chọn ghế trên sơ đồ thật của phòng chiếu, giữ ghế trong thời gian chờ, áp dụng "
             + "mã khuyến mãi, thanh toán và quản lý vé của mình. Quản trị viên theo dõi hoạt động của chuỗi rạp "
             + "gồm phim, rạp, lịch chiếu, đơn đặt vé, khuyến mãi và doanh thu.",
             "**Backend:** Spring Boot 3.5.16, Java 17, Spring Web MVC, Spring Security 6 (form login + "
             + "session), Spring Data JPA / Hibernate 6. Cung cấp REST API `/api/**` và các trang web "
             + "server-side render.",
             "**Frontend:** Thymeleaf 3.1 + thymeleaf-extras-springsecurity6, CSS thuần theo chủ đề tối, "
             + "JavaScript cho thao tác chọn ghế. Không cần Node.js khi triển khai.",
             "**Cơ sở dữ liệu:** MySQL 8, 14 bảng, schema do `database/init.sql` quản lý, Hibernate dùng "
             + "`ddl-auto=validate` nên không tự sửa cấu trúc bảng.",
             "**Triển khai:** Docker Compose gồm 3 dịch vụ – MySQL (mạng nội bộ, không publish cổng), Spring "
             + "Boot (build multi-stage Gradle 8.14 + JDK 17 → JRE 17) và Nginx làm reverse proxy.",
             "**Tích hợp:** TMDB và PayOS có luồng triển khai; chúng chỉ hoạt động khi cấu hình khoá hợp lệ. "
             + "YouTube được dùng để hiển thị trailer khi dữ liệu có sẵn. NVIDIA NIM, Google OAuth và Firebase "
             + "chỉ có cấu hình khung, chưa có luồng chức năng tương ứng."},
    };

    static final String[][] ABBR = {
            {"SRS", "Software Requirements Specification – Tài liệu đặc tả yêu cầu phần mềm"},
            {"UC", "Use Case – Ca sử dụng"},
            {"ERD", "Entity Relationship Diagram – Sơ đồ quan hệ thực thể"},
            {"JPA", "Java Persistence API – Chuẩn ánh xạ quan hệ đối tượng sang bảng quan hệ"},
            {"ORM", "Object-Relational Mapping – Ánh xạ đối tượng Java sang bảng cơ sở dữ liệu"},
            {"MVC", "Model – View – Controller, mô hình kiến trúc của Spring Web"},
            {"SSR", "Server-Side Rendering – Kỹ thuật render giao diện phía máy chủ"},
            {"CSRF", "Cross-Site Request Forgery – Tấn công giả mạo yêu cầu chéo trang"},
            {"BCrypt", "Hàm băm mật khẩu có salt tích hợp, dùng bởi Spring Security"},
            {"Docker", "Nền tảng đóng gói và chạy ứng dụng dưới dạng container"},
            {"Held / Confirmed / Cancelled", "Các trạng thái của một đơn đặt vé (Booking)"},
    };

    // ============================================================== PHẦN 2
    static final String[][] ENTITIES = {
            {"Role", "id, name, description", "Vai trò của tài khoản: admin / user / staff"},
            {"User", "id, name, email, password, phone, avatar, role_id, status, created_at",
             "Tài khoản người dùng hoặc quản trị viên"},
            {"Province", "id, code, name, region, latitude, longitude", "Tỉnh/thành phố, phục vụ nhóm rạp và định vị"},
            {"Cinema", "id, province_id, name, address, phone, lat, lng, district, city, region, brand, status",
             "Chi nhánh rạp thuộc chuỗi CineX"},
            {"Screen", "id, cinema_id, name, screen_type, rows_count, cols_count", "Phòng chiếu thuộc một rạp"},
            {"Seat", "id, screen_id, row_label, seat_number, seat_type, is_active",
             "Ghế thuộc bố cục cố định của phòng (thường / VIP)"},
            {"Movie", "id, tmdb_id, title, original_title, genre, duration, rating, poster, backdrop, director, "
             + "cast_members, description, release_date, ticket_price, vip_price, trailer_url, language, "
             + "popularity, status", "Thông tin phim"},
            {"Showtime", "id, movie_id, cinema_id, screen_id, start_time, end_time, base_price, vip_price, status",
             "Suất chiếu của một phim tại một phòng"},
            {"Booking", "id, code, user_id, showtime_id, total_amount, discount_amount, final_amount, "
             + "promotion_code, payment_method, status", "Đơn đặt vé"},
            {"BookingSeat", "id, booking_id, showtime_id, seat_id, seat_label, price, status",
             "Ghế thuộc một đơn đặt vé; UNIQUE (showtime_id, seat_id) chống bán trùng ghế"},
            {"Ticket", "id, ticket_code, booking_id, user_id, showtime_id, seat_label, qr_code, status, used_at",
             "Vé điện tử phát hành sau khi thanh toán hợp lệ"},
            {"Bill", "id, code, booking_id, user_id, subtotal, discount, total, issued_at", "Hoá đơn của đơn đặt vé"},
            {"Payment", "id, code, booking_id, user_id, provider, amount, status, provider_order_id, "
             + "provider_ref, paid_at", "Giao dịch thanh toán"},
            {"Promotion", "id, code, title, description, discount_type, discount_value, max_discount, "
             + "min_amount, start_date, end_date, usage_limit, used_count, is_active", "Chương trình khuyến mãi"},
    };

    static final String[] RELATIONS = {
            "Role (1) — (N) User: một vai trò phục vụ nhiều tài khoản.",
            "Province (1) — (N) Cinema: một tỉnh/thành có nhiều chi nhánh rạp.",
            "Cinema (1) — (N) Screen: một rạp có nhiều phòng chiếu.",
            "Screen (1) — (N) Seat và Screen (1) — (N) Showtime: bố cục ghế và lịch chiếu gắn với phòng.",
            "Movie (1) — (N) Showtime: một phim có nhiều suất chiếu.",
            "User (1) — (N) Booking: một người dùng có nhiều đơn đặt vé.",
            "Showtime (1) — (N) Booking: một suất chiếu có nhiều đơn đặt vé.",
            "Booking (N) — (N) Seat thông qua BookingSeat.",
            "Booking (1) — (1) Bill: mỗi đơn đặt vé có đúng một hoá đơn.",
            "Booking (1) — (N) Payment: có thể có nhiều lần thử thanh toán.",
            "Booking (1) — (N) Ticket: mỗi ghế trong đơn tương ứng một vé điện tử.",
    };

    static final String[][] BOOKING_STATES = {
            {"Pending", "Ghế đã được giữ tạm sau khi tạo đơn (15 phút)",
             "Confirmed (thanh toán), Cancelled (huỷ)"},
            {"Confirmed", "Đã thanh toán thành công, đã phát hành vé và hoá đơn",
             "Cancelled (huỷ trước giờ chiếu)"},
            {"Cancelled", "Đơn bị huỷ, ghế được trả lại cho suất chiếu", "(kết thúc)"},
    };

    static final String[][] PERM_FUNC = {
            {"Xem danh sách phim / lịch chiếu / hệ thống rạp", "X", "X", "X"},
            {"Đặt vé (chọn ghế, giữ ghế, thanh toán)", "X", "X (thay khách)", "—"},
            {"Áp dụng khuyến mãi", "X", "—", "—"},
            {"Huỷ đơn đặt vé", "X (đơn của mình)", "—", "—"},
            {"Xem lịch sử đặt vé và vé của mình", "X", "—", "—"},
            {"Quản lý hồ sơ, đổi mật khẩu", "X", "—", "X"},
            {"Quản lý phim / rạp / phòng / ghế / suất chiếu", "—", "—", "X"},
            {"Quản lý khuyến mãi và người dùng", "—", "—", "X"},
            {"Xem báo cáo doanh thu và thống kê hoạt động", "—", "—", "X"},
    };

    static final String[][] PERM_DATA = {
            {"Booking (đơn đặt vé)", "Chỉ đọc/sửa đơn của mình", "Đọc đơn theo suất phụ trách", "Toàn quyền đọc/sửa"},
            {"Users (hồ sơ)", "Chỉ sửa hồ sơ của mình", "Không truy cập hồ sơ khách", "Toàn quyền quản lý tài khoản"},
            {"Dữ liệu phim / suất chiếu / ghế", "Chỉ đọc", "Chỉ đọc", "Toàn quyền tạo/sửa/xoá"},
            {"Khuyến mãi", "Chỉ đọc và áp dụng khi thanh toán", "Không áp dụng", "Toàn quyền quản lý"},
            {"Cấu hình tích hợp ngoài / khoá API", "Không truy cập", "Không truy cập", "Chỉ đọc trạng thái đã cấu hình"},
    };

    static final String[] SITEMAP = {
            "Trang chủ (phim đang chiếu / sắp chiếu / lịch chiếu hôm nay / khuyến mãi / hệ thống rạp)",
            "→ Danh sách phim (tìm kiếm, phân trang) → Chi tiết phim → Chọn suất chiếu",
            "→ Hệ thống rạp (nhóm theo tỉnh/thành) → Chi tiết rạp (lịch chiếu tại rạp)",
            "→ Lịch chiếu (lọc theo ngày 1/3/7 ngày và theo rạp)",
            "→ Chọn ghế (sơ đồ phòng) → Thanh toán (khuyến mãi + phương thức thanh toán) → Vé điện tử",
            "Tài khoản: Đăng ký / Đăng nhập → Hồ sơ (đổi mật khẩu) → Vé của tôi (lịch sử, huỷ đơn)",
            "Khu vực Admin: Bảng điều khiển (doanh thu) → Quản lý phim / rạp / đơn đặt vé / khuyến mãi",
    };

    // ============================================================== PHẦN 3
    record Uc(String id, String name, String group, String actors, String priority,
               String desc, String trigger, String pre, String post,
               List<String> basic, List<String> alt, List<String> exc,
               String rules, String nfr, String flow, String ui, String[][] fields) {
    }

    static List<Uc> useCases() {
        List<Uc> list = new ArrayList<>();

        list.add(new Uc("UC1", "Đăng ký / Đăng nhập / Đăng xuất", "Tài khoản",
                "Người dùng, Admin", "Cao",
                "Người dùng tạo tài khoản mới hoặc đăng nhập để sử dụng hệ thống; phiên đăng nhập được thiết lập bằng session phía máy chủ.",
                "Người dùng truy cập trang Đăng ký hoặc Đăng nhập",
                "Đăng ký: email và số điện thoại chưa tồn tại. Đăng nhập: tài khoản đã tồn tại và còn hiệu lực.",
                "Phiên đăng nhập được thiết lập, cookie phiên (JSESSIONID) được cấp; người dùng được điều hướng theo vai trò.",
                List.of(
                        "Đăng ký: nhập họ tên, email, số điện thoại, mật khẩu và xác nhận mật khẩu → kiểm tra trùng email/số điện thoại → băm mật khẩu bằng BCrypt → tạo User với role = user.",
                        "Đăng nhập: nhập email và mật khẩu → Spring Security xác thực → thiết lập session → điều hướng theo vai trò (quản trị về /admin, người dùng về trang chủ).",
                        "Đăng xuất: gửi POST /logout → vô hiệu hoá session, xoá cookie phiên → chuyển về trang chủ."),
                List.of("A1 — Người dùng đã đăng nhập thì truy cập /login hoặc /register được chuyển hướng về trang chủ."),
                List.of(
                        "E1 — Email đã tồn tại → hiển thị lỗi tại trường nhập, không tạo tài khoản.",
                        "E2 — Số điện thoại đã tồn tại → hiển thị lỗi tại trường nhập.",
                        "E3 — Mật khẩu xác nhận không khớp → hiển thị lỗi.",
                        "E4 — Sai email/mật khẩu → chuyển về /login?error kèm thông báo chung, không tiết lộ thông tin tài khoản.",
                        "E5 — Tài khoản bị khoá (status = blocked) → từ chối đăng nhập."),
                "Email và số điện thoại là duy nhất; mật khẩu tối thiểu 6 ký tự; số điện thoại theo định dạng Việt Nam; mật khẩu luôn được lưu ở dạng băm BCrypt.",
                "Bảo vệ CSRF tự động trên mọi biểu mẫu POST; mật khẩu không bao giờ lưu dạng plaintext; phiên hết hạn sau 60 phút không hoạt động.",
                "Truy cập biểu mẫu → validate phía server → kiểm tra trùng lặp → băm mật khẩu → tạo tài khoản hoặc xác thực → thiết lập session → điều hướng theo vai trò.",
                "Trang đăng nhập và đăng ký trung tâm, validate tại server, hiển thị lỗi rõ ràng dưới từng trường, kèm khối gợi ý tài khoản demo.",
                new String[][]{
                        {"Họ và tên", "name", "Text", "Có", "Tên hiển thị, tối đa 100 ký tự"},
                        {"Email", "email", "Email", "Có", "Địa chỉ email, duy nhất, tối đa 100 ký tự"},
                        {"Số điện thoại", "phone", "Text", "Có", "Duy nhất, định dạng 0xx… hoặc +84xx…"},
                        {"Mật khẩu", "password", "Password", "Có", "Tối thiểu 6 ký tự"},
                        {"Xác nhận mật khẩu", "confirmPassword", "Password", "Có", "Phải khớp với mật khẩu"},
                }));

        list.add(new Uc("UC2", "Xem danh sách phim & tìm kiếm", "Xem phim",
                "Người dùng", "Cao",
                "Người dùng xem toàn bộ phim đang chiếu và sắp chiếu, tìm kiếm theo tên hoặc thể loại và duyệt theo trang.",
                "Người dùng truy cập trang Danh sách phim",
                "Có dữ liệu phim hợp lệ trong hệ thống",
                "Hiển thị danh sách phim kèm poster, thể loại, thời lượng, phân loại giới hạn độ tuổi",
                List.of(
                        "Hệ thống truy vấn phim có trạng thái active, sắp xếp theo độ phủ giảm dần.",
                        "Người dùng nhập từ khoá → hệ thống lọc theo tên phim hoặc thể loại (không phân biệt hoa thường).",
                        "Danh sách được chia trang 12 phim/trang, người dùng chuyển trang bằng nút trước/sau."),
                List.of(
                        "A1 — Bỏ trống từ khoá → hiển thị toàn bộ phim đang chiếu.",
                        "A2 — Không có kết quả → hiển thị trạng thái rỗng kèm hướng dẫn."),
                List.of("E1 — Từ khoá chứa ký tự đặc biệt không được hỗ trợ → xử lý như từ khoá thông thường, không lỗi."),
                "Chỉ hiển thị phim có trạng thái active; tìm kiếm không phân biệt hoa thường; phân trang mặc định 12 bản ghi/trang.",
                "Truy vấn phân trang chạy ở mức cơ sở dữ liệu (không tải toàn bộ bảng); trang phải phản hồi dưới 2 giây.",
                "Mở trang phim → nhập từ khoá (tuỳ chọn) → truy vấn Movie có phân trang → hiển thị lưới poster → chọn phim → sang UC3.",
                "Lưới poster phim, ô tìm kiếm, điều hướng phân trang, huy hiệu phân loại giới hạn độ tuổi và thời lượng.",
                new String[][]{
                        {"Từ khoá", "q", "Text", "Không", "Tìm theo tên phim hoặc thể loại"},
                        {"Số trang", "page", "Number", "Không", "Chỉ số trang, bắt đầu từ 0"},
                }));

        list.add(new Uc("UC3", "Xem chi tiết phim & lịch chiếu của phim", "Xem phim",
                "Người dùng", "Cao",
                "Người dùng xem thông tin đầy đủ của một bộ phim và danh sách suất chiếu còn có thể đặt vé.",
                "Người dùng chọn một phim từ trang danh sách hoặc trang chủ",
                "Phim tồn tại và có trạng thái active",
                "Hiển thị nội dung phim kèm lịch chiếu theo rạp, phòng và giờ chiếu",
                List.of(
                        "Hệ thống tải phim theo id cùng toàn bộ suất chiếu sắp tới của phim.",
                        "Hiển thị poster, tiêu đề, đạo diễn, diễn viên, thể loại, thời lượng, mô tả, ngày phát hành, giá vé thường và giá VIP, đường dẫn trailer.",
                        "Danh sách suất chiếu hiển thị rạp, phòng, khung giờ bắt đầu – kết thúc và ngày chiếu.",
                        "Chọn suất chiếu → chuyển sang UC6 (chọn ghế)."),
                List.of(
                        "A1 — Phim chưa có trailer → ẩn nút xem trailer.",
                        "A2 — Phim chưa có suất chiếu → hiển thị trạng thái rỗng kèm hướng dẫn."),
                List.of("E1 — Không tìm thấy phim → chuyển về trang chủ kèm thông báo “Không tìm thấy phim”."),
                "Chỉ hiển thị suất chiếu chưa diễn ra; giá vé hiển thị theo giá của suất chiếu, không dùng giá cố định toàn cục.",
                "Chi tiết phim phải phản hồi dưới 2 giây; ảnh poster tải lazy và có kích thước dự trữ để tránh nhảy layout.",
                "Chọn phim → tải phim và suất chiếu → hiển thị trang chi tiết → chọn suất → sang UC6.",
                "Cột poster bên trái, thông tin phim và bảng đặc tính bên phải, khối lịch chiếu dạng danh sách nút giờ chiếu ở cuối trang.",
                new String[][]{
                        {"Mã phim", "id", "Number", "Có", "Định danh phim trong đường dẫn /movies/{id}"},
                }));

        list.add(new Uc("UC4", "Xem hệ thống rạp & lịch chiếu tại rạp", "Xem phim",
                "Người dùng", "Trung bình",
                "Người dùng xem toàn bộ chi nhánh rạp của chuỗi CineX theo tỉnh/thành và xem lịch chiếu riêng của một chi nhánh.",
                "Người dùng truy cập trang Rạp chiếu phim",
                "Có dữ liệu chi nhánh rạp hợp lệ",
                "Hiển thị các chi nhánh theo tỉnh/thành và lịch chiếu của chi nhánh được chọn",
                List.of(
                        "Hệ thống tải toàn bộ chi nhánh có trạng thái active, nhóm theo tỉnh/thành.",
                        "Hiển thị tên chi nhánh, địa chỉ, thương hiệu và thành phố.",
                        "Chọn một chi nhánh → xem lịch chiếu sắp tới tại rạp đó (phim, phòng, giờ, ngày)."),
                List.of("A1 — Chi nhánh chưa có suất chiếu → hiển thị trạng thái rỗng."),
                List.of("E1 — Không tìm thấy chi nhánh → chuyển về trang chủ kèm thông báo."),
                "Toàn bộ rạp trong hệ thống cùng thuộc thương hiệu CineX; hệ thống không hiển thị rạp của chuỗi khác; chỉ hiển thị suất chiếu chưa diễn ra.",
                "Trang rạp phải phản hồi dưới 3 giây với 45 chi nhánh; dùng phân trang hoặc nhóm theo tỉnh để giữ trang nhẹ.",
                "Mở trang rạp → nhóm theo tỉnh → chọn chi nhánh → xem lịch chiếu → chọn suất → sang UC6.",
                "Tiêu đề theo tỉnh/thành, lưới thẻ rạp; trang chi tiết rạp có danh sách nút giờ chiếu.",
                new String[][]{
                        {"Mã rạp", "id", "Number", "Có", "Định danh chi nhánh trong /cinemas/{id}"},
                }));

        list.add(new Uc("UC5", "Xem lịch chiếu theo ngày", "Xem phim",
                "Người dùng", "Cao",
                "Người dùng xem lịch chiếu theo ngày, lọc theo khoảng 1–7 ngày và theo chi nhánh rạp.",
                "Người dùng truy cập trang Lịch chiếu",
                "Có dữ liệu suất chiếu tương lai",
                "Hiển thị lịch chiếu theo từng ngày, nhóm theo chi nhánh",
                List.of(
                        "Mặc định lấy lịch chiếu của ngày hôm nay, cho phép chọn khoảng 1, 3 hoặc 7 ngày.",
                        "Người dùng chọn chi nhánh trong hộp chọn để lọc; có thể chọn ngày bắt đầu.",
                        "Suất chiếu được nhóm theo ngày, mỗi ngày nhóm theo chi nhánh, mỗi chi nhánh là danh sách nút giờ.",
                        "Chọn giờ chiếu → sang UC6."),
                List.of(
                        "A1 — Bỏ chọn chi nhánh → hiển thị lịch chiếu của toàn bộ chuỗi rạp.",
                        "A2 — Không có suất chiếu trong khoảng đã chọn → hiển thị trạng thái rỗng."),
                List.of("E1 — Ngày chọn không hợp lệ → hệ thống dùng ngày hiện tại."),
                "Chỉ hiển thị suất chiếu có trạng thái scheduled và còn nằm trong khoảng thời gian đã chọn; khoảng lọc tối đa 7 ngày để bảo đảm hiệu năng.",
                "Phản hồi dưới 2 giây; khoảng lọc mặc định 1 ngày để trang luôn nhẹ.",
                "Mở trang lịch chiếu → chọn ngày/khoảng ngày → chọn rạp → truy vấn suất chiếu → nhóm theo ngày → chọn giờ → sang UC6.",
                "Thanh bộ lọc (ngày, khoảng ngày, rạp), các nhóm ngày với tiêu đề thứ – ngày và lưới nút giờ.",
                new String[][]{
                        {"Ngày bắt đầu", "date", "Date", "Không", "Mặc định ngày hiện tại"},
                        {"Số ngày", "days", "Select", "Không", "1, 3 hoặc 7 ngày"},
                        {"Chi nhánh rạp", "cinemaId", "Select", "Không", "Lọc theo rạp, mặc định tất cả"},
                }));

        list.add(new Uc("UC6", "Chọn ghế trên sơ đồ phòng", "Đặt vé",
                "Người dùng", "Cao",
                "Người dùng xem sơ đồ ghế thật của phòng chiếu, chọn ghế trống và theo dõi tổng tiền tạm tính.",
                "Người dùng chọn một suất chiếu",
                "Suất chiếu còn hiệu lực; người dùng đã đăng nhập nếu muốn tiến hành đặt vé",
                "Danh sách ghế đã chọn và tổng tiền tạm tính hiển thị trên giao diện",
                List.of(
                        "Hệ thống truy vấn bố cục ghế của phòng (theo hàng A..H, số ghế tăng dần) kèm trạng thái ghế đã bán hoặc đang giữ của suất chiếu.",
                        "Ghế được phân loại: ghế thường (màu trung tính) và ghế VIP (2 hàng cuối, viền xanh).",
                        "Người dùng bấm ghế trống → ghế chuyển sang trạng thái đang chọn; bấm lần nữa để bỏ chọn.",
                        "Tổng tiền và số ghế được cập nhật tức thời; nút thanh toán chỉ kích hoạt khi có ít nhất 1 ghế."),
                List.of(
                        "A1 — Ghế đã bán → bấm không có hiệu lực, hiển thị chú thích “Ghế đã bán”.",
                        "A2 — Không còn ghế trống → hiển thị sơ đồ toàn ghế đã bán và hướng dẫn chọn suất khác."),
                List.of("E1 — Suất chiếu không tồn tại hoặc đã bắt đầu → chuyển về trang chủ kèm thông báo."),
                "Mỗi suất chiếu có bố cục ghế riêng theo phòng; chỉ chọn được ghế còn trống; giá mỗi ghế phụ thuộc loại ghế (thường/VIP) theo giá của suất chiếu.",
                "Sơ đồ ghế phản hồi dưới 2 giây; thao tác chọn ghế phản hồi tức thời phía trình duyệt (không tải lại trang); hỗ trợ hiển thị tốt trên màn hình điện thoại.",
                "Chọn suất chiếu → tải sơ đồ ghế → đánh dấu ghế đã bán → người dùng chọn ghế → cập nhật tổng tiền → chuyển sang UC7.",
                "Khối thông tin suất chiếu bên trái gồm tóm tắt đơn và nút thanh toán; sơ đồ ghế dạng lưới theo hàng, có vạch màn hình, chú thích màu loại ghế.",
                new String[][]{
                        {"Mã suất chiếu", "showtimeId", "Number", "Có", "Suất chiếu cần chọn ghế"},
                        {"Danh sách ghế", "seatIds", "List<Integer>", "Có", "Mã ghế được chọn, gửi lên server khi thanh toán"},
                        {"Phương thức thanh toán", "paymentMethod", "Select", "Không", "cash / card / transfer"},
                }));

        list.add(new Uc("UC7", "Giữ ghế & tạo đơn đặt vé", "Đặt vé",
                "Người dùng", "Cao nhất",
                "Người dùng gửi danh sách ghế đã chọn; hệ thống kiểm tra tính hợp lệ, giữ ghế và tạo đơn đặt vé ở trạng thái chờ thanh toán.",
                "Người dùng bấm nút “Tiến hành thanh toán” ở trang chọn ghế",
                "Người dùng đã đăng nhập; suất chiếu chưa bắt đầu; ghế thuộc đúng phòng của suất chiếu",
                "Tạo Booking ở trạng thái pending kèm các BookingSeat; người dùng được chuyển tới trang thanh toán với mã đơn",
                List.of(
                        "Server nhận showtimeId và danh sách seatIds.",
                        "Kiểm tra suất chiếu tồn tại và chưa bắt đầu.",
                        "Kiểm tra toàn bộ ghế tồn tại và thuộc đúng phòng của suất chiếu.",
                        "Tạo Booking với mã dạng BK + thời gian + số ngẫu nhiên, trạng thái pending.",
                        "Ghi từng ghế vào BookingSeat với giá theo loại ghế và tổng tiền tạm tính.",
                        "Chuyển hướng tới trang thanh toán kèm mã đơn."),
                List.of("A1 — Người dùng chưa đăng nhập → chuyển tới trang đăng nhập kèm thông báo yêu cầu đăng nhập."),
                List.of(
                        "E1 — Suất chiếu đã bắt đầu → từ chối tạo đơn.",
                        "E2 — Ghế không tồn tại hoặc thuộc phòng khác → từ chối tạo đơn, không ghi dữ liệu.",
                        "E3 — Ghế vừa bị người khác giữ → ràng buộc UNIQUE (showtime_id, seat_id) chặn ghi trùng, giao dịch được hủy và ghế không bị chiếm."),
                "Mỗi suất chiếu chỉ có thể giữ mỗi ghế một lần; ghế giữ tối đa 15 phút; tổng tiền do server tính lại từ dữ liệu ghế, không tin giá trị gửi lên từ trình duyệt.",
                "Toàn bộ thao tác tạo đơn nằm trong một giao dịch cơ sở dữ liệu; ràng buộc khóa ngoại và ràng buộc duy nhất bảo đảm không bán trùng ghế kể cả khi có truy cập đồng thời.",
                "Gửi ghế đã chọn → kiểm tra suất chiếu → kiểm tra ghế → tạo Booking + BookingSeat → chuyển sang UC8.",
                "Trang thông tin suất chiếu và tóm tắt đơn hàng bên trái, sơ đồ ghế bên phải; nút thanh toán nằm trong khối tóm tắt.",
                new String[][]{
                        {"Mã suất chiếu", "showtimeId", "Number", "Có", "Suất chiếu được chọn"},
                        {"Danh sách ghế", "seatIds", "List<Integer>", "Có", "Tối thiểu 1 ghế"},
                        {"Mã khuyến mãi", "promotionCode", "Text", "Không", "Áp dụng ở bước thanh toán"},
                }));

        list.add(new Uc("UC8", "Áp dụng khuyến mãi & thanh toán", "Thanh toán",
                "Người dùng", "Cao nhất",
                "Người dùng xác nhận thanh toán đơn đặt vé; hệ thống tính lại giá, áp dụng mã khuyến mãi, chuyển đơn sang đã thanh toán và phát hành vé điện tử kèm hoá đơn.",
                "Người dùng bấm nút “Xác nhận thanh toán”",
                "Đơn đặt vé ở trạng thái pending và thuộc về người dùng đang đăng nhập",
                "Đơn chuyển sang confirmed; phát hành vé điện tử theo từng ghế, tạo hoá đơn và giao dịch thanh toán; tăng bộ đếm sử dụng của mã khuyến mãi",
                List.of(
                        "Server tải Booking theo mã đơn và kiểm tra quyền sở hữu.",
                        "Tính lại tổng tiền từ danh sách BookingSeat của đơn.",
                        "Nếu có mã khuyến mãi: kiểm tra mã tồn tại, còn hiệu lực, đạt đơn tối thiểu và còn lượt dùng → tính mức giảm theo phần trăm hoặc số tiền cố định, không vượt mức giảm tối đa.",
                        "Cập nhật Booking: tổng tiền, mức giảm, tổng thanh toán, phương thức thanh toán, trạng thái confirmed.",
                        "Chuyển các BookingSeat sang trạng thái paid.",
                        "Phát hành Ticket cho từng ghế kèm mã vé và mã QR.",
                        "Tạo Bill (hoá đơn) và Payment với trạng thái paid."),
                List.of(
                        "A1 — Không nhập mã khuyến mãi → thanh toán với giá gốc.",
                        "A2 — Phương thức thanh toán tiền mặt tại quầy → ghi nhận ngay, không gọi cổng thanh toán ngoài."),
                List.of(
                        "E1 — Mã khuyến mãi không tồn tại → từ chối thanh toán, đơn giữ nguyên trạng thái pending.",
                        "E2 — Mã khuyến mãi hết hạn, hết lượt dùng hoặc đơn chưa đạt mức tối thiểu → từ chối tương ứng.",
                        "E3 — Đơn đã được xử lý trước đó → từ chối xử lý lại (chống thanh toán hai lần).",
                        "E4 — Người dùng không sở hữu đơn → từ chối, chuyển về trang vé của tôi."),
                "Giá và mức giảm luôn do server tính lại; mức giảm phần trăm bị giới hạn bởi max_discount; mức giảm không được vượt quá tổng tiền đơn; mỗi đơn chỉ có một hoá đơn.",
                "Thao tác xác nhận thanh toán nằm trong một giao dịch, đảm bảo đơn, ghế, vé và hoá đơn luôn nhất quán; không phát hành vé trùng khi bấm nút nhiều lần.",
                "Xác nhận → kiểm tra quyền và trạng thái → tính lại giá → áp khuyến mãi → chuyển trạng thái → phát hành vé + hoá đơn → hiển thị kết quả.",
                "Khối tóm tắt thanh toán hiển thị tổng tiền vé, mức giảm khuyến mãi và tổng cộng; biểu mẫu nhập mã khuyến mãi và chọn phương thức thanh toán; khối hoá đơn hiển thị mã hoá đơn.",
                new String[][]{
                        {"Mã đơn", "code", "Text", "Có", "Mã Booking sinh từ server"},
                        {"Mã khuyến mãi", "promotionCode", "Text", "Không", "Kiểm tra hiệu lực trước khi áp dụng"},
                        {"Phương thức thanh toán", "paymentMethod", "Select", "Có", "cash / card / transfer"},
                }));

        list.add(new Uc("UC9", "Xem vé của tôi & huỷ đơn", "Tài khoản",
                "Người dùng", "Trung bình",
                "Người dùng xem lịch sử đơn đặt vé, xem chi tiết và có thể gửi yêu cầu huỷ đơn; mã hiện tại không chặn huỷ đơn đã thanh toán.",
                "Người dùng truy cập menu “Vé của tôi”",
                "Người dùng đã đăng nhập",
                "Hiển thị danh sách đơn kèm trạng thái; khi huỷ, ghế được trả lại cho suất chiếu",
                List.of(
                        "Hệ thống truy vấn các đơn đặt vé của người dùng theo thời gian giảm dần.",
                        "Hiển thị mã đơn, phim, rạp, giờ chiếu, tổng tiền và trạng thái (chờ thanh toán / đã thanh toán / đã huỷ).",
                        "Chọn một đơn → xem chi tiết gồm ghế đã chọn, vé đã phát hành và mã hoá đơn.",
                        "Huỷ đơn → các BookingSeat chuyển sang trạng thái released, Booking chuyển sang cancelled."),
                List.of("A1 — Chưa có đơn nào → hiển thị trạng thái rỗng kèm nút dẫn tới danh sách phim."),
                List.of(
                        "E1 — Người dùng không sở hữu đơn → chuyển về trang vé của tôi kèm thông báo.",
                        "E2 — Đơn không tồn tại → báo lỗi; cần bổ sung quy tắc nghiệp vụ để giới hạn huỷ đơn đã thanh toán.",),
                "Chỉ người sở hữu đơn hoặc admin được thao tác đơn; khi huỷ, ghế được giải phóng. Quy tắc huỷ đơn đã thanh toán cần được xác định trước vận hành thực tế.",
                "Danh sách đơn phản hồi dưới 2 giây; thao tác huỷ đơn xác nhận lại trước khi thực thi.",
                "Mở trang vé của tôi → truy vấn đơn theo user → hiển thị danh sách → mở chi tiết → (huỷ) xác nhận → cập nhật trạng thái và trả ghế.",
                "Danh sách dạng thẻ: thông tin phim/rạp/giờ bên trái, tổng tiền và trạng thái bên phải, nút “Chi tiết” cho từng đơn.",
                new String[][]{
                        {"Mã đơn", "code", "Text", "Có", "Mã Booking để mở chi tiết"},
                }));

        list.add(new Uc("UC10", "Quản lý hồ sơ & đổi mật khẩu", "Tài khoản",
                "Người dùng", "Thấp",
                "Người dùng cập nhật họ tên, số điện thoại và đổi mật khẩu của tài khoản mình.",
                "Người dùng truy cập trang Hồ sơ",
                "Người dùng đã đăng nhập",
                "Hồ sơ được cập nhật hoặc mật khẩu được thay đổi, có thông báo kết quả",
                List.of(
                        "Hệ thống hiển thị hồ sơ hiện tại kèm ngày đăng ký và vai trò.",
                        "Cập nhật hồ sơ: sửa họ tên, số điện thoại → kiểm tra số điện thoại không trùng tài khoản khác → lưu.",
                        "Đổi mật khẩu: nhập mật khẩu cũ và mật khẩu mới → đối chiếu mật khẩu cũ → băm và lưu mật khẩu mới."),
                List.of("A1 — Không nhập số điện thoại mới → giữ nguyên số điện thoại cũ."),
                List.of(
                        "E1 — Số điện thoại đã thuộc tài khoản khác → từ chối cập nhật.",
                        "E2 — Mật khẩu cũ không đúng → từ chối đổi mật khẩu.",
                        "E3 — Mật khẩu mới ngắn hơn 6 ký tự → từ chối."),
                "Người dùng chỉ sửa được hồ sơ của chính mình; email không được tự sửa trong phạm vi hiện hành.",
                "Mật khẩu mới được băm bằng BCrypt; thao tác nhạy cảm ghi log ở mức ứng dụng.",
                "Mở trang hồ sơ → chỉnh sửa → validate → lưu → thông báo thành công.",
                "Hai thẻ: thông tin tài khoản (kèm ảnh đại diện chữ cái đầu) và biểu mẫu đổi mật khẩu.",
                new String[][]{
                        {"Họ và tên", "name", "Text", "Có", "Tên hiển thị"},
                        {"Số điện thoại", "phone", "Text", "Không", "Phải duy nhất trong hệ thống"},
                        {"Mật khẩu cũ", "oldPassword", "Password", "Có (đổi mật khẩu)", "Đối chiếu trước khi đổi"},
                        {"Mật khẩu mới", "newPassword", "Password", "Có (đổi mật khẩu)", "Tối thiểu 6 ký tự"},
                }));

        list.add(new Uc("UC11", "Xem khuyến mãi", "Khuyến mãi",
                "Người dùng", "Thấp",
                "Người dùng xem các chương trình khuyến mãi đang hiệu lực để lấy mã áp dụng khi thanh toán.",
                "Người dùng truy cập trang Khuyến mãi",
                "Có chương trình khuyến mãi đang hoạt động",
                "Hiển thị mã, mức giảm, đơn tối thiểu và hạn dùng của từng chương trình",
                List.of(
                        "Hệ thống tải các khuyến mãi đang bật, còn trong hạn, còn lượt dùng.",
                        "Hiển thị mã, tiêu đề, mô tả, mức giảm, đơn tối thiểu và ngày hết hạn."),
                List.of("A1 — Không có khuyến mãi nào → hiển thị trạng thái rỗng."),
                List.of("E1 — Không xảy ra lỗi trong phạm vi chức năng."),
                "Chỉ hiển thị chương trình đang bật và còn hiệu lực tại ngày xem.",
                "Trang phản hồi dưới 2 giây; mã khuyến mãi hiển thị dạng chữ để dễ sao chép.",
                "Mở trang khuyến mãi → truy vấn chương trình đang hiệu lực → hiển thị danh sách.",
                "Lưới thẻ khuyến mãi, mã hiển thị nổi bật, thông tin đơn tối thiểu và hạn dùng ở chân thẻ.",
                new String[][]{}));

        list.add(new Uc("UC12", "Xem bảng điều khiển & báo cáo doanh thu", "Quản trị",
                "Admin", "Trung bình",
                "Quản trị viên xem tổng quan hoạt động của chuỗi rạp: số phim, số chi nhánh, số suất chiếu, số đơn đặt vé và doanh thu theo tuần và theo thương hiệu.",
                "Quản trị viên truy cập trang Bảng điều khiển",
                "Đã đăng nhập với vai trò admin",
                "Hiển thị các thẻ số liệu, bảng doanh thu theo thương hiệu và danh sách đơn đặt vé gần đây",
                List.of(
                        "Hệ thống tổng hợp số liệu từ phim, rạp, phòng, suất chiếu, đơn đặt vé và thanh toán.",
                        "Doanh thu tuần này được so sánh với tuần trước và hiển thị xu hướng tăng/giảm.",
                        "Doanh thu 7 ngày gần nhất được tổng hợp theo từng ngày.",
                        "Doanh thu được nhóm theo thương hiệu rạp và theo số đơn.",
                        "Danh sách 10 đơn đặt vé gần nhất kèm trạng thái được hiển thị."),
                List.of("A1 — Chưa có đơn nào → doanh thu bằng 0, bảng hiển thị trạng thái rỗng."),
                List.of("E1 — Người dùng không có vai trò admin → bị chuyển về trang đăng nhập kèm thông báo không đủ quyền."),
                "Chỉ tính doanh thu từ đơn có trạng thái confirmed; đơn cancelled không tính.",
                "Truy vấn tổng hợp phản hồi dưới 3 giây; truy vấn tổng hợp chạy ở mức cơ sở dữ liệu, không tải toàn bộ bảng booking vào bộ nhớ.",
                "Mở /admin → gọi dịch vụ tổng hợp số liệu → tính doanh thu theo khoảng tuần → hiển thị thẻ số liệu + bảng + danh sách đơn gần đây.",
                "Lưới thẻ số liệu (phim, chi nhánh, suất chiếu, đơn đặt vé, doanh thu tuần), bảng doanh thu theo thương hiệu, bảng đơn đặt vé gần đây.",
                new String[][]{}));

        list.add(new Uc("UC13", "Quản lý phim", "Quản trị",
                "Admin", "Trung bình",
                "Quản trị viên xem danh sách phim kèm thông tin chi tiết (hạng mục thêm/xoá sẽ được bổ sung cùng hạng mục đồng bộ TMDB).",
                "Quản trị viên truy cập trang Quản lý phim",
                "Đã đăng nhập với vai trò admin",
                "Hiển thị danh sách phim kèm poster, thể loại, thời lượng, ngày phát hành, giá vé, độ phủ",
                List.of(
                        "Hệ thống truy vấn phim kèm phân trang 20 bản ghi/trang.",
                        "Hiển thị poster thu nhỏ, tên phim, đạo diễn, thể loại, thời lượng, ngày phát hành, giá vé và độ phủ.",
                        "Chọn một phim để xem trang chi tiết công khai."),
                List.of("A1 — Chưa có phim → hiển thị trạng thái rỗng."),
                List.of("E1 — Người dùng không có vai trò admin → bị từ chối truy cập."),
                "Mọi thao tác quản trị chỉ dành cho vai trò admin; người dùng chỉ đọc.",
                "Danh sách phân trang ở mức cơ sở dữ liệu; phản hồi dưới 3 giây.",
                "Mở /admin/movies → truy vấn phim có phân trang → hiển thị bảng → chuyển tới trang chi tiết phim.",
                "Bảng dữ liệu có cột poster, tên phim (liên kết tới trang công khai), đạo diễn, thể loại, thời lượng, ngày phát hành, giá vé, độ phủ.",
                new String[][]{}));

        list.add(new Uc("UC14", "Quản lý hệ thống rạp", "Quản trị",
                "Admin", "Trung bình",
                "Quản trị viên xem toàn bộ chi nhánh rạp của chuỗi theo tỉnh/thành kèm địa chỉ và thương hiệu.",
                "Quản trị viên truy cập trang Quản lý rạp",
                "Đã đăng nhập với vai trò admin",
                "Hiển thị danh sách chi nhánh nhóm theo tỉnh/thành",
                List.of(
                        "Hệ thống tải toàn bộ chi nhánh có trạng thái active, nhóm theo tỉnh/thành.",
                        "Hiển thị tên chi nhánh, địa chỉ, thương hiệu và liên kết xem trang công khai của rạp."),
                List.of("A1 — Không có rạp → hiển thị trạng thái rỗng."),
                List.of("E1 — Người dùng không có vai trò admin → bị từ chối truy cập."),
                "Chỉ hiển thị chi nhánh của chuỗi CineX; hệ thống không quản lý rạp của chuỗi khác.",
                "Trang phản hồi dưới 3 giây với 45 chi nhánh.",
                "Mở /admin/cinemas → truy vấn rạp → nhóm theo tỉnh → hiển thị bảng theo từng nhóm.",
                "Tiêu đề theo tỉnh/thành, bảng gồm tên chi nhánh, địa chỉ, thương hiệu và nút xem.",
                new String[][]{}));

        list.add(new Uc("UC15", "Quản lý đơn đặt vé", "Quản trị",
                "Admin", "Trung bình",
                "Quản trị viên theo dõi toàn bộ đơn đặt vé của hệ thống kèm khách hàng, phim, rạp và trạng thái.",
                "Quản trị viên truy cập trang Quản lý đơn đặt vé",
                "Đã đăng nhập với vai trò admin",
                "Hiển thị danh sách đơn đặt vé kèm thông tin khách hàng và trạng thái",
                List.of(
                        "Hệ thống truy vấn toàn bộ đơn đặt vé có phân trang 20 bản ghi/trang.",
                        "Hiển thị mã đơn, tên khách hàng, phim, rạp, giờ chiếu, tổng tiền và trạng thái."),
                List.of("A1 — Chưa có đơn nào → hiển thị trạng thái rỗng."),
                List.of("E1 — Người dùng không có vai trò admin → bị từ chối truy cập."),
                "Quản trị viên xem được toàn bộ đơn; người dùng chỉ xem đơn của mình.",
                "Phân trang ở mức cơ sở dữ liệu; phản hồi dưới 3 giây.",
                "Mở /admin/bookings → truy vấn đơn có phân trang → hiển thị bảng.",
                "Bảng 7 cột: mã đơn, khách hàng, phim, rạp, suất chiếu, tổng tiền, trạng thái có màu.",
                new String[][]{}));

        list.add(new Uc("UC16", "Quản lý khuyến mãi", "Quản trị",
                "Admin", "Trung bình",
                "Quản trị viên xem danh sách chương trình khuyến mãi, mức chiết khấu, điều kiện áp dụng và trạng thái sử dụng.",
                "Quản trị viên truy cập trang Quản lý khuyến mãi",
                "Đã đăng nhập với vai trò admin",
                "Hiển thị danh sách khuyến mãi kèm số lượt đã dùng và trạng thái",
                List.of(
                        "Hệ thống tải toàn bộ khuyến mãi theo thứ tự tạo giảm dần.",
                        "Hiển thị mã, tiêu đề, mức chiết khấu, đơn tối thiểu, hạn dùng, số lượt đã dùng và trạng thái.",
                        "Chọn một khuyến mãi → xem trang chi tiết cấu hình."),
                List.of("A1 — Chưa có khuyến mãi → hiển thị trạng thái rỗng."),
                List.of("E1 — Không tìm thấy khuyến mãi → chuyển về danh sách kèm thông báo."),
                "Mã khuyến mãi là duy nhất; mức giảm phần trăm không vượt quá max_discount; mức giảm không vượt quá tổng tiền đơn; số lượt dùng không vượt quá usage_limit.",
                "Tính mức giảm thực hiện ở tầng nghiệp vụ (Promotion) và được kiểm thử bằng unit test.",
                "Mở /admin/promotions → truy vấn khuyến mãi → hiển thị bảng → mở trang chi tiết.",
                "Bảng 7 cột: mã, tiêu đề, chiết khấu, đơn tối thiểu, hạn dùng, đã dùng, trạng thái.",
                new String[][]{}));

        return list;
    }

    static final String[][] ROADMAP = {
            {"UC17", "Trợ lý AI hội thoại (NVIDIA NIM)", "Đặt vé", "Người dùng",
             "Hội thoại tư vấn và điều hướng quy trình đặt vé bằng ngôn ngữ tự nhiên, có bộ luật dự phòng.",
             "Chưa có service và giao diện chat."},
            {"UC18", "API JWT + xác thực OTP / OAuth", "Tài khoản", "Người dùng, Admin",
             "Cấp access/refresh token cho client ngoài, đăng nhập bằng OTP email/SMS, Google OAuth, Firebase.",
             "Hiện chỉ dùng session của Spring Security; chưa có luồng token, OTP hay OAuth."},
            {"UC19", "Bán kèm bắp nước / dịch vụ đi kèm", "Đặt vé", "Người dùng, Admin",
             "Thêm combo vào đơn đặt vé, quản lý tồn kho và giá, kiểm tra giá ở server để chống giả giá.",
             "Chưa có bảng dữ liệu concessions trong khung hiện tại."},
            {"UC20", "Quét vé điện tử tại rạp (check-in)", "Vận hành rạp", "Nhân viên rạp",
             "Nhân viên quét mã QR, hệ thống xác thực vé và chuyển trạng thái sang đã soát.",
             "Vé đã có mã QR sinh sẵn; chưa có giao diện quét và lịch sử soát vé."},
    };

    // ============================================================== PHẦN 4
    static final String[][] NOTIFICATIONS = {
            {"NB01", "Thành công", "“Đăng ký thành công. Chào mừng {name} đến với CineX!”", "Sau UC1 khi tạo tài khoản"},
            {"NB02", "Thành công", "“Đã giữ {n} ghế. Hoàn tất thanh toán trong 15 phút.”", "Sau UC7 khi tạo đơn thành công"},
            {"NB03", "Thành công", "“Thanh toán thành công. Mã đơn: {code}”", "Sau UC8 khi xác nhận thanh toán"},
            {"NB04", "Thành công", "“Cập nhật hồ sơ thành công”", "Sau UC10 khi lưu hồ sơ"},
            {"NB05", "Thành công", "“Đổi mật khẩu thành công”", "Sau UC10 khi đổi mật khẩu"},
            {"NB06", "Thành công", "“Đã huỷ đơn đặt vé và trả lại ghế.”", "Sau UC9 khi huỷ đơn"},
            {"NB07", "Cảnh báo", "“Ghế bạn chọn sắp hết hạn giữ, vui lòng thanh toán trong {mm:ss}.”",
             "Trong UC8 khi đơn sắp quá hạn"},
            {"NB08", "Cảnh báo", "“Không thể huỷ vé do đã quá thời hạn cho phép trước giờ chiếu.”",
             "UC9 luồng ngoại lệ"},
            {"NB09", "Lỗi", "“Suất chiếu này đã bắt đầu, không thể đặt vé.”", "UC7 luồng ngoại lệ E1"},
            {"NB10", "Lỗi", "“Ghế {label} không thuộc phòng của suất chiếu.”", "UC7 luồng ngoại lệ E2"},
            {"NB11", "Lỗi", "“Ghế vừa được người khác đặt. Vui lòng chọn ghế khác.”", "UC7 luồng ngoại lệ E3"},
            {"NB12", "Lỗi", "“Mã khuyến mãi không tồn tại.”", "UC8 luồng ngoại lệ E1"},
            {"NB13", "Lỗi", "“Mã khuyến mãi không còn hiệu lực.”", "UC8 luồng ngoại lệ E2"},
            {"NB14", "Lỗi", "“Email này đã được đăng ký. Vui lòng dùng email khác.”", "UC1 luồng ngoại lệ E1"},
            {"NB15", "Lỗi", "“Email hoặc mật khẩu không đúng.”", "UC1 luồng ngoại lệ E4"},
            {"NB16", "Lỗi", "“Đã xảy ra lỗi, vui lòng thử lại.”", "Mọi lỗi không lường trước"},
            {"NB17", "Thông tin", "“Bạn không có quyền xem đơn đặt vé này.”", "UC9 luồng ngoại lệ E1"},
            {"NB18", "Xác nhận", "“Bạn có chắc muốn huỷ đơn {code}?”", "UC9 trước khi huỷ đơn"},
    };

    // ============================================================== PHẦN 5
    static final String[][] NFR = {
            {"5.1 Hiệu năng", """
                    Phản hồi của các trang web và API nội bộ dưới 2 giây trong điều kiện tải thông thường.
                    Trang danh sách phim và danh sách đơn đặt vé phải phân trang ở mức cơ sở dữ liệu, không tải toàn bộ bảng.
                    Sơ đồ ghế phản hồi dưới 2 giây; thao tác chọn ghế phản hồi tức thời phía trình duyệt.
                    Truy vấn tổng hợp doanh thu phản hồi dưới 3 giây.
                    Các tích hợp ngoài (PayOS, TMDB, YouTube, NVIDIA) khi được bật phải có timeout, xử lý lỗi và không làm chậm luồng đặt vé.""".strip()},
            {"5.2 Bảo mật", """
                    Mật khẩu lưu ở dạng băm BCrypt, không bao giờ lưu dạng plaintext; mật khẩu tối thiểu 6 ký tự.
                    Bảo vệ CSRF bật mặc định trên mọi biểu mẫu POST; session dùng cookie HttpOnly.
                    Phân quyền hiện hành gồm user và admin qua Spring Security; các chức năng nhân viên rạp chưa có giao diện hoặc quyền tác nghiệp riêng.
                    Người dùng chỉ truy cập được đơn đặt vé của chính mình, kiểm tra ở tầng service chứ không chỉ ở giao diện.
                    Giá vé, mức giảm và tổng tiền luôn được server tính lại; không tin giá trị gửi lên từ trình duyệt.
                    Khoá tích hợp bên ngoài chỉ đọc từ biến môi trường, file `.env` nằm trong `.gitignore`; endpoint trạng thái tích hợp không trả về giá trị khoá.""".strip()},
            {"5.3 Độ tin cậy & toàn vẹn dữ liệu", """
                    Tạo đơn đặt vé và xác nhận thanh toán thực hiện trong giao dịch cơ sở dữ liệu.
                    Ràng buộc UNIQUE (showtime_id, seat_id) trên bảng booking_seats chống bán trùng ghế kể cả khi có nhiều yêu cầu đồng thời.
                    Ràng buộc khóa ngoại giữa phim – suất chiếu – đơn – ghế – vé – thanh toán bảo đảm tính toàn vẹn tham chiếu.
                    Hibernate dùng `ddl-auto=validate`: schema chỉ được quản lý bởi `database/init.sql`, ứng dụng không tự sửa cấu trúc bảng khi khởi động.
                    Job định kỳ chạy mỗi 60 giây và giải phóng ghế của booking pending sau 15 phút; trạng thái đơn hết hạn được lưu là cancelled.""".strip()},
            {"5.4 Khả dụng & trải nghiệm", """
                    Giao diện responsive, dùng được trên điện thoại và máy tính; chủ đề tối, tương phản đạt yêu cầu đọc.
                    Thông báo lỗi rõ ràng, có hướng dẫn khắc phục, theo danh mục Phần 4.
                    Mọi thao tác huỷ hoặc thanh toán đều yêu cầu xác nhận lại.
                    Trang lịch chiếu mặc định chỉ tải 1 ngày để giữ tốc độ; người dùng tự mở rộng 3 hoặc 7 ngày.
                    Hệ thống phải phục hồi được sau khi khởi động lại mà không mất dữ liệu (dữ liệu nằm trong volume MySQL).""".strip()},
            {"5.5 Khả năng bảo trì & mở rộng", """
                    Kiến trúc tách tầng rõ ràng: controller – service – repository – domain, dễ kiểm thử và thay thế.
                    Cấu hình tích hợp bên ngoài gom trong một lớp `IntegrationProperties` để bổ sung dịch vụ mới không phải sửa mã nghiệp vụ.
                    Dữ liệu mẫu nạp bằng `DataSeeder` theo cơ chế idempotent, tắt được bằng `APP_SEED_ENABLED=false`.
                    Build hoàn toàn bằng Docker nhiều giai đoạn; máy chủ không cần cài JDK, Gradle hay Node.js.
                    Có sẵn Gradle wrapper để chạy `./gradlew build` trên máy phát triển có JDK 17.
                    Tài liệu SRS và các sơ đồ được sinh tự động bằng Java thuần (`docs/SrsGenerator.java`), không phụ thuộc công cụ ngoài.""".strip()},
            {"5.6 Yêu cầu dữ liệu & khởi tạo", """
                    Hệ thống khởi tạo 45 chi nhánh rạp, 90 phòng chiếu, 7.020 ghế, 14 phim, 2.520 suất chiếu, 3 khuyến mãi và 63 tỉnh/thành.
                    Địa chỉ chi nhánh lấy từ mạng lưới rạp cùng hệ thống trước đây, đổi thương hiệu thành CineX; dữ liệu nằm trong `init/CinemaSeed.java`.
                    Sơ đồ ERD gồm 14 thực thể được mô tả tại mục 2.1.""".strip()},
            {"5.7 Yêu cầu triển khai & vận hành", """
                    Triển khai bằng `docker compose --env-file .env up -d --build`; các biến môi trường bắt buộc gồm cấu hình MySQL và cổng publish.
                    Kiểm tra tình trạng ứng dụng qua `GET /api/health`; kiểm tra tình trạng tích hợp qua `GET /api/public/integrations`.
                    Sao lưu dữ liệu bằng cách dump volume MySQL định kỳ.
                    Quy trình kiểm thử chất lượng: `./gradlew test` (unit test), `./gradlew build` (đóng gói), kiểm thử tay trọn luồng đặt vé trên môi trường đóng gói.""".strip()},
    };

    // ============================================================== PHẦN 6
    static final String[][] IMPLEMENTATION = {
            {"UC1", "Đăng ký / đăng nhập / đăng xuất",
             "controller/web/AuthController.java; service/AuthService.java; config/SecurityConfig.java", "Hoàn thành"},
            {"UC1", "Băm mật khẩu BCrypt, phiên đăng nhập, bảo vệ CSRF",
             "config/SecurityConfig.java; templates/auth/login.html; templates/auth/register.html", "Hoàn thành"},
            {"UC2", "Danh sách phim, tìm kiếm, phân trang",
             "controller/web/MovieController.java; repository/MovieRepository.java; templates/movies/list.html", "Hoàn thành"},
            {"UC3", "Chi tiết phim và lịch chiếu của phim",
             "controller/web/MovieController.java; service/ShowtimeService.java; templates/movies/detail.html", "Hoàn thành"},
            {"UC4", "Hệ thống rạp nhóm theo tỉnh và lịch chiếu tại rạp",
             "controller/web/CinemaController.java; service/CinemaService.java; templates/cinemas/", "Hoàn thành"},
            {"UC5", "Lịch chiếu theo ngày và theo rạp",
             "controller/web/ShowtimeController.java; templates/showtimes/schedule.html", "Hoàn thành"},
            {"UC6", "Sơ đồ ghế, chọn ghế, tính tổng tiền",
             "controller/web/SeatController.java; service/SeatService.java; static/js/seats.js; templates/booking/seats.html", "Hoàn thành"},
            {"UC7", "Giữ ghế, tạo đơn đặt vé trong giao dịch",
             "service/BookingService.java; domain/BookingSeat.java (ràng buộc UNIQUE)", "Hoàn thành"},
            {"UC8", "Tính giá lại, áp khuyến mãi, phát hành vé và hoá đơn",
             "service/BookingService.java; domain/Promotion.java; templates/booking/detail.html", "Hoàn thành"},
            {"UC9", "Vé của tôi, chi tiết đơn, huỷ đơn trả lại ghế",
             "controller/web/BookingController.java; templates/booking/my-tickets.html", "Hoàn thành"},
            {"UC10", "Quản lý hồ sơ và đổi mật khẩu",
             "controller/web/ProfileController.java; service/AuthService.java; templates/profile.html", "Hoàn thành"},
            {"UC11", "Trang khuyến mãi công khai",
             "controller/web/PromotionController.java; templates/promotions.html", "Hoàn thành"},
            {"UC12", "Bảng điều khiển và báo cáo doanh thu",
             "service/AdminService.java; controller/web/AdminController.java; templates/admin/dashboard.html", "Hoàn thành"},
            {"UC13–16", "Các trang quản trị (phim, rạp, đơn đặt vé, khuyến mãi)",
             "controller/web/AdminController.java; templates/admin/", "Hoàn thành (chỉ xem)"},
            {"UC13–16", "Biểu mẫu thêm / sửa / xoá trong khu vực quản trị",
             "service/PromotionService.java (đã có hàm create/update/delete)", "Mở rộng – chưa có controller"},
            {"UC7.5", "Job định kỳ giải phóng ghế khi đơn quá hạn",
             "service/BookingService.java#releaseExpiredHolds; CinexApplication.java", "Hoàn thành – chạy mỗi 60 giây"},
            {"UC17–20", "AI hội thoại, JWT/OTP/OAuth, bắp nước, check-in QR",
             "config/IntegrationProperties.java (đã có cấu hình)", "Mở rộng – chưa triển khai"},
            {"—", "Kiểm thử tự động: chiết khấu khuyến mãi, nhãn ghế",
             "src/test/java/com/cinex/domain/", "Hoàn thành – 7 test đạt"},
            {"—", "Sinh tài liệu SRS và sơ đồ bằng Java thuần",
             "docs/SrsGenerator.java", "Hoàn thành"},
    };

    static final String[][] COMPARISON = {
            {"Kiến trúc backend", "Node.js + Express 5", "Spring Boot 3.5.16 + Java 17", "Đã chuyển"},
            {"Giao diện", "React 19 + Vite 8 (SPA)", "Thymeleaf 3.1 (render phía máy chủ)", "Đã chuyển"},
            {"Xác thực", "JWT access + refresh + blacklist", "Session của Spring Security", "Đã chuyển, JWT là mở rộng UC20"},
            {"Cơ sở dữ liệu", "29 bảng (schema không chuẩn hoá, seats nhân bản theo suất)",
             "14 bảng chuẩn hoá (seats theo phòng, booking_seats tách riêng)", "Đã chuyển"},
            {"Phạm vi rạp", "Nhiều chuỗi rạp (CGV, Lotte, Galaxy…)", "Một chuỗi duy nhất (CineX), 45 chi nhánh", "Theo yêu cầu mới"},
            {"Trợ lý AI hội thoại", "Có (NVIDIA NIM, 67 KB service + 30 KB widget)", "Chưa có, đã có chỗ cấu hình", "Mở rộng UC18"},
            {"Thanh toán online", "PayOS (link thanh toán + webhook xác minh chữ ký)", "PayOS có triển khai, cần cấu hình khoá và webhook công khai", "Đã có luồng"},
            {"Đồng bộ phim", "TMDB định kỳ + YouTube trailer", "Đồng bộ TMDB theo thao tác admin; trailer dùng dữ liệu phim", "Đã có luồng, cần cấu hình khoá"},
            {"Bắp nước / concessions", "Có (tồn kho, chống giả giá)", "Chưa có", "Mở rộng UC21"},
            {"Check-in quét QR", "Chưa có (vé QR đã sinh)", "Chưa có (vé QR đã sinh)", "Mở rộng UC22"},
            {"Số endpoint", "83", "35", "—"},
            {"Số bảng dữ liệu", "29", "14", "—"},
    };

    // ========================================================== MÔ HÌNH KHỐI
    /** Một khối nội dung dùng chung cho Markdown, HTML và DOCX. */
    interface Block {
    }

    record Head(int level, String text) implements Block {
    }

    record Paragraph(String text) implements Block {
    }

    record Table(String[] headers, List<String[]> rows) implements Block {
    }

    /** @param file tên file PNG trong thư mục ảnh; @param caption chú thích dưới ảnh */
    record Image(String file, String caption) implements Block {
    }

    record PageBreak() implements Block {
    }

    record Spacer() implements Block {
    }

    static List<Block> buildBlocks() {
        List<Block> b = new ArrayList<>();
        List<Uc> ucs = useCases();

        // ---------- Bìa ----------
        b.add(new Head(1, DOC_TITLE));
        b.add(new Paragraph(DOC_SUBTITLE));
        b.add(new Paragraph(DOC_SUBTITLE2));
        b.add(new Table(new String[]{"Trường", "Nội dung"}, rows(META)));
        b.add(new Head(3, "Lịch sử phiên bản"));
        b.add(new Table(new String[]{"Phiên bản", "Ngày", "Người thực hiện", "Nội dung"}, rows(VERSIONS)));
        b.add(new Head(3, "Tài liệu liên quan, tham khảo"));
        b.add(new Table(new String[]{"Ngày", "Tên tài liệu", "Nguồn"}, rows(REFS)));
        b.add(new PageBreak());

        // ---------- Mục lục ----------
        b.add(new Head(2, "MỤC LỤC"));
        b.add(new Table(new String[]{"Phần", "Nội dung"}, rows(new String[][]{
                {"PHẦN 1", "GIỚI THIỆU"},
                {"PHẦN 2", "YÊU CẦU TỔNG THỂ"},
                {"PHẦN 3", "CHỨC NĂNG (UC1 – UC20)"},
                {"PHẦN 4", "CÁC COMPONENT, THÔNG BÁO, CẢNH BÁO"},
                {"PHẦN 5", "YÊU CẦU PHI CHỨC NĂNG TỔNG THỂ"},
                {"PHẦN 6", "ĐỐI CHIẾU TRIỂN KHAI"},
                {"PHẦN 7", "ĐỐI CHIẾU VỚI HỆ THỐNG EXPRESS/REACT"},
        })));
        b.add(new PageBreak());

        // ---------- PHẦN 1 ----------
        b.add(new Head(2, "PHẦN 1: GIỚI THIỆU"));
        for (String[] sec : P1_TEXT) {
            b.add(new Head(3, sec[0]));
            for (int i = 1; i < sec.length; i++) {
                b.add(new Paragraph(sec[i]));
            }
        }
        b.add(new Head(3, "1.4 Thuật ngữ viết tắt"));
        b.add(new Table(new String[]{"STT", "Từ viết tắt", "Diễn giải"}, rows(abbrRows())));

        // ---------- PHẦN 2 ----------
        b.add(new PageBreak());
        b.add(new Head(2, "PHẦN 2: YÊU CẦU TỔNG THỂ"));

        b.add(new Head(3, "2.1 Sơ đồ quan hệ đối tượng (ERD)"));
        b.add(new Image("erd.png", "Hình 2.1 – Sơ đồ ERD hệ thống CineX"));
        b.add(new Table(new String[]{"Thực thể (Entity)", "Các thuộc tính chính", "Ý nghĩa"}, rows(ENTITIES)));
        b.add(new Paragraph("Các quan hệ chính (relationships):"));
        for (String r : RELATIONS) {
            b.add(new Paragraph("• " + r));
        }

        b.add(new Head(3, "2.2 Sơ đồ Use Case"));
        b.add(new Image("usecase.png", "Hình 2.2 – Sơ đồ Use Case theo phạm vi hiện hành và mở rộng"));
        b.add(new Paragraph("Hệ thống có 3 tác nhân: **Người dùng** (đăng ký, xem phim, đặt vé, quản lý vé "
                + "của mình), **Nhân viên rạp** (vai trò `staff`, đã có trong bảng dữ liệu) và **Admin** "
                + "(quản lý phim, rạp, suất chiếu, khuyến mãi, người dùng và theo dõi đơn đặt vé)."));
        List<String[]> ucRows = new ArrayList<>();
        for (Uc u : ucs) {
            ucRows.add(new String[]{u.id(), u.name(), u.group(), u.actors(), "Đã triển khai"});
        }
        for (String[] r : ROADMAP) {
            ucRows.add(new String[]{r[0], r[1], r[2], r[3], "Mở rộng"});
        }
        b.add(new Table(new String[]{"Mã", "Tên chức năng", "Phân hệ", "Tác nhân", "Trạng thái"}, ucRows));

        b.add(new Head(3, "2.3 Sơ đồ luồng"));
        b.add(new Image("flow.png", "Hình 2.3 – Luồng đặt vé qua giao diện Thymeleaf"));
        b.add(new Paragraph("Luồng đặt vé cốt lõi (UC6 → UC7 → UC8):"));
        String[] flowSteps = {
                "Người dùng chọn suất chiếu từ trang chủ / trang phim / trang lịch chiếu.",
                "Hệ thống dựng sơ đồ ghế của phòng và đánh dấu ghế đã bán hoặc đang giữ.",
                "Người dùng chọn ghế; giao diện tự tính tổng tiền tạm tính.",
                "Gửi danh sách ghế lên server; server kiểm tra suất chiếu và ghế, tạo đơn ở trạng thái pending.",
                "Người dùng nhập mã khuyến mãi (nếu có) và chọn phương thức thanh toán.",
                "Server tính lại giá, áp khuyến mãi, chuyển đơn sang confirmed, phát hành vé và hoá đơn.",
        };
        for (int i = 0; i < flowSteps.length; i++) {
            b.add(new Paragraph((i + 1) + ". " + flowSteps[i]));
        }

        b.add(new Head(3, "2.4 Sơ đồ chuyển trạng thái"));
        b.add(new Image("state.png", "Hình 2.4 – Sơ đồ chuyển trạng thái Booking"));
        b.add(new Paragraph("Vòng đời của một đơn đặt vé (Booking):"));
        b.add(new Table(new String[]{"Trạng thái", "Mô tả", "Chuyển đến"}, rows(BOOKING_STATES)));

        b.add(new Head(3, "2.5 Phân quyền"));
        b.add(new Head(4, "2.5.1 Phân quyền chức năng"));
        b.add(new Table(new String[]{"Chức năng", "Người dùng", "Nhân viên rạp", "Admin"}, rows(PERM_FUNC)));
        b.add(new Head(4, "2.5.2 Phân quyền dữ liệu"));
        b.add(new Table(new String[]{"Đối tượng dữ liệu", "Người dùng", "Nhân viên rạp", "Admin"}, rows(PERM_DATA)));

        b.add(new Head(3, "2.6 Site Map"));
        b.add(new Image("sitemap.png", "Hình 2.5 – Site Map hệ thống"));
        for (String s : SITEMAP) {
            b.add(new Paragraph("• " + s));
        }

        // ---------- PHẦN 3 ----------
        b.add(new PageBreak());
        b.add(new Head(2, "PHẦN 3: CHỨC NĂNG"));
        for (int i = 0; i < ucs.size(); i++) {
            Uc u = ucs.get(i);
            int n = i + 1;
            b.add(new Head(3, "3." + n + ". " + u.id() + " — " + u.name()));
            b.add(new Head(4, "3." + n + ".1. Đặc tả Use Case"));
            b.add(new Table(new String[]{"Thuộc tính", "Nội dung"}, rows(new String[][]{
                    {"Use Case ID", u.id()},
                    {"Mô tả", u.desc()},
                    {"Tác nhân (Actor(s))", u.actors()},
                    {"Sự ưu tiên (Priority)", u.priority()},
                    {"Trigger", u.trigger()},
                    {"Điều kiện cần (Pre-Condition)", u.pre()},
                    {"Điều kiện sau (Post-Condition(s))", u.post()},
                    {"Luồng cơ bản (Basic Flow)", numbered(u.basic())},
                    {"Luồng thay thế (Alternative Flow)", u.alt().isEmpty() ? "—" : String.join("\n", u.alt())},
                    {"Luồng ngoại lệ (Exception Flow)", u.exc().isEmpty() ? "—" : String.join("\n", u.exc())},
                    {"Ràng buộc (Business Rules)", u.rules()},
                    {"Yêu cầu phi chức năng (Non-Functional Requirement)", u.nfr()},
            })));
            b.add(new Head(4, "3." + n + ".2. Sơ đồ luồng chi tiết"));
            b.add(new Paragraph(u.flow()));
            b.add(new Head(4, "3." + n + ".3. Giao diện"));
            b.add(new Paragraph(u.ui()));
            if (u.fields().length > 0) {
                b.add(new Head(4, "3." + n + ".4. Mô tả chi tiết"));
                b.add(new Table(new String[]{"Tên tiếng Việt", "Tên tiếng Anh", "Loại", "Bắt buộc?", "Mô tả"},
                        rows(u.fields())));
            }
        }
        b.add(new Head(3, "3." + (ucs.size() + 1) + ". Các use case mở rộng (chưa triển khai)"));
        b.add(new Paragraph("Các hạng mục dưới đây nằm trong phạm vi dự kiến của hệ thống nhưng "
                + "**chưa có route và giao diện**. Khung hiện tại đã chuẩn bị sẵn chỗ cấu hình cho "
                + "các dịch vụ liên quan."));
        b.add(new Table(new String[]{"Mã", "Tên chức năng", "Phân hệ", "Tác nhân", "Mô tả", "Hiện trạng"},
                rows(ROADMAP)));

        // ---------- PHẦN 4 ----------
        b.add(new PageBreak());
        b.add(new Head(2, "PHẦN 4: CÁC COMPONENT, THÔNG BÁO, CẢNH BÁO"));
        b.add(new Paragraph("Danh sách các mẫu thông báo, cảnh báo sử dụng trong hệ thống:"));
        b.add(new Table(new String[]{"Mã", "Loại", "Nội dung mẫu", "Ngữ cảnh xuất hiện"}, rows(NOTIFICATIONS)));

        // ---------- PHẦN 5 ----------
        b.add(new PageBreak());
        b.add(new Head(2, "PHẦN 5: YÊU CẦU PHI CHỨC NĂNG TỔNG THỂ"));
        for (String[] sec : NFR) {
            b.add(new Head(3, sec[0]));
            for (String line : sec[1].split("\n")) {
                b.add(new Paragraph("• " + line));
            }
        }

        // ---------- PHẦN 6 ----------
        b.add(new PageBreak());
        b.add(new Head(2, "PHẦN 6: ĐỐI CHIẾU TRIỂN KHAI"));
        b.add(new Paragraph("Bảng đối chiếu từng hạng mục trong đặc tả với mã nguồn hiện có trong thư mục "
                + "`project 19-23`:"));
        b.add(new Table(new String[]{"Mã UC", "Hạng mục", "File / vị trí cài đặt", "Trạng thái"}, rows(IMPLEMENTATION)));

        // ---------- PHẦN 7 ----------
        b.add(new PageBreak());
        b.add(new Head(2, "PHẦN 7: ĐỐI CHIẾU VỚI HỆ THỐNG EXPRESS/REACT"));
        b.add(new Paragraph("Bảng so sánh giữa hệ thống cũ (Node.js + React) và bản Spring Boot hiện tại:"));
        b.add(new Table(new String[]{"Hạng mục", "Hệ thống cũ (Express/React)", "Hệ thống mới (Spring Boot)",
                "Ghi chú"}, rows(COMPARISON)));
        b.add(new Paragraph("Tài liệu được sinh tự động bằng Java thuần (`docs/SrsGenerator.java`). "
                + "Cập nhật: " + DOC_DATE + "."));

        return b;
    }

    static List<String[]> rows(String[][] data) {
        return new ArrayList<>(List.of(data));
    }

    static String[][] abbrRows() {
        String[][] r = new String[ABBR.length][3];
        for (int i = 0; i < ABBR.length; i++) {
            r[i] = new String[]{String.valueOf(i + 1), ABBR[i][0], ABBR[i][1]};
        }
        return r;
    }

    static String numbered(List<String> items) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            sb.append(i + 1).append(". ").append(items.get(i));
            if (i < items.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    // ============================================================== MARKDOWN
    static String renderMarkdown(List<Block> blocks) {
        StringBuilder b = new StringBuilder();
        for (Block blk : blocks) {
            if (blk instanceof Head h) {
                b.append("#".repeat(h.level())).append(" ").append(h.text()).append("\n\n");
            } else if (blk instanceof Paragraph p) {
                b.append(p.text().replace("\n", "  \n")).append("\n\n");
            } else if (blk instanceof Table t) {
                b.append(mdTable(t.headers(), t.rows()));
            } else if (blk instanceof Image im) {
                b.append("![").append(im.caption()).append("](img/").append(im.file()).append(")\n\n");
            } else if (blk instanceof PageBreak) {
                b.append("---\n\n");
            }
        }
        return b.toString();
    }

    static String mdTable(String[] headers, List<String[]> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("| ").append(String.join(" | ", headers)).append(" |\n");
        sb.append("|").append("---|".repeat(headers.length)).append("\n");
        for (String[] r : data) {
            sb.append("| ");
            for (int i = 0; i < r.length; i++) {
                sb.append(i > 0 ? " | " : "").append(r[i].replace("\n", "<br>"));
            }
            sb.append(" |\n");
        }
        return sb.append("\n").toString();
    }

    // ================================================================= HTML
    static String renderHtml(List<Block> blocks) {
        StringBuilder b = new StringBuilder();
        b.append("<!DOCTYPE html><html lang='vi'><head><meta charset='utf-8'>")
                .append("<title>SRS - Hệ thống đặt vé xem phim CineX (Spring Boot)</title><style>")
                .append("""
@page { size: A4; margin: 2cm 1.6cm; }
body { font-family:"Liberation Serif","DejaVu Serif",serif; font-size:11pt; line-height:1.5; color:#1a1a1a; }
h1 { font-family:"Liberation Sans",Arial,sans-serif; font-size:19pt; color:#b00020; text-align:center; margin:0 0 6pt; }
h2 { font-family:"Liberation Sans",Arial,sans-serif; font-size:14pt; color:#b00020;
     border-bottom:2px solid #b00020; padding-bottom:4pt; margin-top:24pt; page-break-after:avoid; }
h3 { font-family:"Liberation Sans",Arial,sans-serif; font-size:12.5pt; color:#1f3864; margin-top:16pt; page-break-after:avoid; }
h4 { font-family:"Liberation Sans",Arial,sans-serif; font-size:11.5pt; color:#333; margin-top:12pt; page-break-after:avoid; }
p { margin:5pt 0; text-align:justify; }
table { border-collapse:collapse; width:100%; margin:8pt 0 12pt; font-size:9.5pt; page-break-inside:avoid; }
th { background:#e8e8ee; border:1px solid #999; padding:4pt 6pt; text-align:left;
     font-family:"Liberation Sans",Arial,sans-serif; }
td { border:1px solid #999; padding:4pt 6pt; vertical-align:top; }
ul,ol { margin:5pt 0 8pt 0; padding-left:20pt; }
li { margin:2pt 0; }
code { font-family:"DejaVu Sans Mono",monospace; font-size:9pt; background:#f2f2f2; padding:0 2pt; }
img { max-width:100%; margin:8pt 0; border:1px solid #ccc; }
strong { color:#000; }
.pagebreak { page-break-before:always; }
.caption { font-size:9.5pt; color:#555; text-align:center; margin-top:-6pt; margin-bottom:12pt; }
""")
                .append("</style></head><body>");

        for (Block blk : blocks) {
            if (blk instanceof Head h) {
                b.append("<h").append(h.level()).append(">").append(esc(h.text())).append("</h")
                        .append(h.level()).append(">");
            } else if (blk instanceof Paragraph p) {
                b.append("<p>").append(htmlParagraph(p.text())).append("</p>");
            } else if (blk instanceof Table t) {
                b.append("<table><thead><tr>");
                for (String h : t.headers()) {
                    b.append("<th>").append(esc(h)).append("</th>");
                }
                b.append("</tr></thead><tbody>");
                for (String[] r : t.rows()) {
                    b.append("<tr>");
                    for (String c : r) {
                        b.append("<td>").append(htmlParagraph(c)).append("</td>");
                    }
                    b.append("</tr>");
                }
                b.append("</tbody></table>");
            } else if (blk instanceof Image im) {
                b.append("<img src='img/").append(im.file()).append("' alt='")
                        .append(esc(im.caption())).append("'>")
                        .append("<div class='caption'>").append(esc(im.caption())).append("</div>");
            } else if (blk instanceof PageBreak) {
                b.append("<div class='pagebreak'></div>");
            }
        }
        b.append("</body></html>");
        return b.toString();
    }

    static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    /** Chuyển **đậm** và `code` trong văn bản thành thẻ HTML. */
    static String htmlParagraph(String text) {
        StringBuilder out = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            if (text.startsWith("**", i)) {
                int end = text.indexOf("**", i + 2);
                if (end > 0) {
                    out.append("<b>").append(esc(text.substring(i + 2, end))).append("</b>");
                    i = end + 2;
                    continue;
                }
            }
            if (text.charAt(i) == '`') {
                int end = text.indexOf('`', i + 1);
                if (end > 0) {
                    out.append("<code>").append(esc(text.substring(i + 1, end))).append("</code>");
                    i = end + 1;
                    continue;
                }
            }
            if (text.startsWith("\n", i)) {
                out.append("<br>");
                i++;
                continue;
            }
            out.append(esc(String.valueOf(text.charAt(i))));
            i++;
        }
        return out.toString();
    }

    // ================================================================= DOCX
    /** Ghi tài liệu OOXML (WordprocessingML) trực tiếp, không cần thư viện ngoài. */
    static final class DocxWriter {

        private static final long EMU_PER_CM = 360000L;
        private static final long EMU_PER_PX = 9525L;   // 96 dpi
        private static final long MAX_IMG_CM = 16L;

        private final StringBuilder body = new StringBuilder();
        private final Map<String, byte[]> media = new LinkedHashMap<>();
        private final List<String> imageRels = new ArrayList<>();
        private int relCounter = 2;   // rId1 dành cho styles.xml
        private int drawId = 1;

        void head(int level, String text) {
            body.append("<w:p><w:pPr><w:pStyle w:val=\"Heading").append(level)
                    .append("\"/></w:pPr>")
                    .append(runs(text, true))
                    .append("</w:p>");
        }

        void paragraph(String text) {
            body.append("<w:p>").append(runs(text, false)).append("</w:p>");
        }

        void pageBreak() {
            body.append("<w:p><w:r><w:br w:type=\"page\"/></w:r></w:p>");
        }

        void table(String[] headers, List<String[]> data) {
            int cols = headers.length;
            int total = 9360;                       // chiều rộng nội dung (twip)
            int base = total / cols;
            int[] widths = new int[cols];
            for (int i = 0; i < cols; i++) {
                widths[i] = (i == cols - 1) ? total - base * (cols - 1) : base;
            }

            body.append("<w:tbl><w:tblPr><w:tblStyle w:val=\"TableGrid\"/>")
                    .append("<w:tblW w:w=\"5000\" w:type=\"pct\"/>")
                    .append("<w:tblBorders>")
                    .append("<w:top w:val=\"single\" w:sz=\"4\" w:color=\"999999\"/>")
                    .append("<w:left w:val=\"single\" w:sz=\"4\" w:color=\"999999\"/>")
                    .append("<w:bottom w:val=\"single\" w:sz=\"4\" w:color=\"999999\"/>")
                    .append("<w:right w:val=\"single\" w:sz=\"4\" w:color=\"999999\"/>")
                    .append("<w:insideH w:val=\"single\" w:sz=\"4\" w:color=\"999999\"/>")
                    .append("<w:insideV w:val=\"single\" w:sz=\"4\" w:color=\"999999\"/>")
                    .append("</w:tblBorders></w:tblPr><w:tblGrid>");
            for (int w : widths) {
                body.append("<w:gridCol w:w=\"").append(w).append("\"/>");
            }
            body.append("</w:tblGrid>");

            // Hàng tiêu đề
            body.append("<w:tr><w:trPr><w:tblHeader/></w:trPr>");
            for (int i = 0; i < cols; i++) {
                cell(headers[i], widths[i], "E8E8EE", true);
            }
            body.append("</w:tr>");

            // Các hàng dữ liệu
            for (String[] r : data) {
                body.append("<w:tr>");
                for (int i = 0; i < cols; i++) {
                    cell(i < r.length ? r[i] : "", widths[i], null, false);
                }
                body.append("</w:tr>");
            }
            body.append("</w:tbl>");
            body.append("<w:p/>");
        }

        private void cell(String text, int width, String shade, boolean bold) {
            body.append("<w:tc><w:tcPr><w:tcW w:w=\"").append(width).append("\" w:type=\"dxa\"/>");
            if (shade != null) {
                body.append("<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"").append(shade).append("\"/>");
            }
            body.append("<w:vAlign w:val=\"center\"/></w:tcPr>");
            // Mỗi dòng trong ô là một đoạn văn riêng
            String[] lines = text.split("\n", -1);
            for (String line : lines) {
                body.append("<w:p><w:pPr><w:spacing w:after=\"20\"/></w:pPr>")
                        .append(runs(line, bold))
                        .append("</w:p>");
            }
            body.append("</w:tc>");
        }

        void image(Path png, int widthPx, int heightPx, String caption) {
            String name = png.getFileName().toString();
            try {
                media.put(name, Files.readAllBytes(png));
            } catch (IOException e) {
                throw new IllegalStateException("Không đọc được ảnh " + png, e);
            }
            String rid = "rId" + (relCounter++);
            imageRels.add("<Relationship Id=\"" + rid + "\" Type=\"http://schemas.openxmlformats.org/"
                    + "officeDocument/2006/relationships/image\" Target=\"media/" + name + "\"/>");

            long cx = widthPx * EMU_PER_PX;          // 1 px @ 96 dpi = 9525 EMU
            long cy = heightPx * EMU_PER_PX;
            long maxCx = MAX_IMG_CM * EMU_PER_CM;
            if (cx > maxCx) {
                cy = cy * maxCx / cx;
                cx = maxCx;
            }

            body.append("<w:p><w:pPr><w:jc w:val=\"center\"/></w:pPr><w:r><w:drawing>")
                    .append("<wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\">")
                    .append("<wp:extent cx=\"").append(cx).append("\" cy=\"").append(cy).append("\"/>")
                    .append("<wp:docPr id=\"").append(drawId++).append("\" name=\"")
                    .append(esc(name)).append("\"/>")
                    .append("<a:graphic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">")
                    .append("<a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">")
                    .append("<pic:pic xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">")
                    .append("<pic:nvPicPr><pic:cNvPr id=\"").append(drawId)
                    .append("\" name=\"").append(esc(name)).append("\"/><pic:cNvPicPr/></pic:nvPicPr>")
                    .append("<pic:blipFill><a:blip r:embed=\"").append(rid)
                    .append("\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>")
                    .append("<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"").append(cx)
                    .append("\" cy=\"").append(cy).append("\"/></a:xfrm>")
                    .append("<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr>")
                    .append("</pic:pic></a:graphicData></a:graphic></wp:inline>")
                    .append("</w:drawing></w:r></w:p>");

            if (caption != null && !caption.isBlank()) {
                body.append("<w:p><w:pPr><w:jc w:val=\"center\"/><w:rPr><w:i/><w:color w:val=\"555555\"/>")
                        .append("<w:sz w:val=\"18\"/></w:rPr></w:pPr>")
                        .append(runs(caption, false))
                        .append("</w:p>");
            }
        }

        /** Xuất các đoạn văn, hỗ trợ **đậm** và `code`. */
        private String runs(String text, boolean bold) {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            while (i < text.length()) {
                String props = "<w:rPr>";
                if (bold) {
                    props += "<w:b/>";
                }
                props += "<w:rFonts w:ascii=\"Liberation Sans\" w:hAnsi=\"Liberation Sans\"/>";
                props += "</w:rPr>";

                if (text.startsWith("**", i)) {
                    int end = text.indexOf("**", i + 2);
                    if (end > 0) {
                        sb.append("<w:r>").append(props).append("<w:b/>")
                                .append("<w:t xml:space=\"preserve\">")
                                .append(esc(text.substring(i + 2, end)))
                                .append("</w:t></w:r>");
                        i = end + 2;
                        continue;
                    }
                }
                if (text.charAt(i) == '`') {
                    int end = text.indexOf('`', i + 1);
                    if (end > 0) {
                        sb.append("<w:r>").append(props)
                                .append("<w:rFonts w:ascii=\"Liberation Mono\" w:hAnsi=\"Liberation Mono\"/>")
                                .append("<w:sz w:val=\"18\"/>")
                                .append("<w:t xml:space=\"preserve\">")
                                .append(esc(text.substring(i + 1, end)))
                                .append("</w:t></w:r>");
                        i = end + 1;
                        continue;
                    }
                }
                sb.append("<w:r>").append(props)
                        .append("<w:t xml:space=\"preserve\">")
                        .append(esc(String.valueOf(text.charAt(i))))
                        .append("</w:t></w:r>");
                i++;
            }
            return sb.toString();
        }

        private static final String CONTENT_TYPES = """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                <Default Extension="xml" ContentType="application/xml"/>
                <Default Extension="png" ContentType="image/png"/>
                <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
                <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
                </Types>
                """;

        private static final String ROOT_RELS = """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
                </Relationships>
                """;

        private static final String STYLES = """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
                <w:docDefaults><w:rPrDefault><w:rPr>
                <w:rFonts w:ascii="Liberation Sans" w:hAnsi="Liberation Sans" w:eastAsia="Liberation Sans"/>
                <w:sz w:val="22"/><w:szCs w:val="22"/></w:rPr></w:rPrDefault></w:docDefaults>
                <w:style w:type="paragraph" w:default="1" w:styleId="Normal"><w:name w:val="Normal"/></w:style>
                <w:style w:type="paragraph" w:styleId="Heading1"><w:name w:val="heading 1"/>
                  <w:pPr><w:spacing w:before="240" w:after="120"/><w:jc w:val="center"/></w:pPr>
                  <w:rPr><w:b/><w:color w:val="B00020"/><w:sz w:val="38"/></w:rPr></w:style>
                <w:style w:type="paragraph" w:styleId="Heading2"><w:name w:val="heading 2"/>
                  <w:pPr><w:spacing w:before="280" w:after="120"/>
                    <w:pBdr><w:bottom w:val="single" w:sz="12" w:color="B00020"/></w:pBdr></w:pPr>
                  <w:rPr><w:b/><w:color w:val="B00020"/><w:sz w:val="30"/></w:rPr></w:style>
                <w:style w:type="paragraph" w:styleId="Heading3"><w:name w:val="heading 3"/>
                  <w:pPr><w:spacing w:before="240" w:after="80"/></w:pPr>
                  <w:rPr><w:b/><w:color w:val="1F3864"/><w:sz w:val="26"/></w:rPr></w:style>
                <w:style w:type="paragraph" w:styleId="Heading4"><w:name w:val="heading 4"/>
                  <w:pPr><w:spacing w:before="180" w:after="60"/></w:pPr>
                  <w:rPr><w:b/><w:color w:val="333333"/><w:sz w:val="24"/></w:rPr></w:style>
                <w:style w:type="table" w:styleId="TableGrid"><w:name w:val="Table Grid"/>
                  <w:tblPr><w:tblBorders>
                    <w:top w:val="single" w:sz="4" w:color="999999"/>
                    <w:left w:val="single" w:sz="4" w:color="999999"/>
                    <w:bottom w:val="single" w:sz="4" w:color="999999"/>
                    <w:right w:val="single" w:sz="4" w:color="999999"/>
                    <w:insideH w:val="single" w:sz="4" w:color="999999"/>
                    <w:insideV w:val="single" w:sz="4" w:color="999999"/>
                  </w:tblBorders></w:tblPr></w:style>
                </w:styles>
                """;

        void write(Path target) throws IOException {
            String document = """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
                     xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
                     xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"
                     xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
                     xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture">
                    <w:body>""" + body + """
                    <w:sectPr><w:pgSz w:w="11906" w:h="16838"/>
                    <w:pgMar w:top="1134" w:right="1021" w:bottom="1134" w:left="1021"
                     w:header="709" w:footer="709" w:gutter="0"/></w:sectPr>
                    </w:body></w:document>
                    """;

            StringBuilder rels = new StringBuilder();
            rels.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
                    .append("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">")
                    .append("<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>");
            for (String r : imageRels) {
                rels.append(r);
            }
            rels.append("</Relationships>");

            try (OutputStream out = Files.newOutputStream(target);
                 ZipOutputStream zip = new ZipOutputStream(out)) {
                put(zip, "[Content_Types].xml", CONTENT_TYPES.getBytes(StandardCharsets.UTF_8));
                put(zip, "_rels/.rels", ROOT_RELS.getBytes(StandardCharsets.UTF_8));
                put(zip, "word/document.xml", document.getBytes(StandardCharsets.UTF_8));
                put(zip, "word/_rels/document.xml.rels", rels.toString().getBytes(StandardCharsets.UTF_8));
                put(zip, "word/styles.xml", STYLES.getBytes(StandardCharsets.UTF_8));
                for (Map.Entry<String, byte[]> e : media.entrySet()) {
                    put(zip, "word/media/" + e.getKey(), e.getValue());
                }
            }
        }

        private static void put(ZipOutputStream zip, String name, byte[] data) throws IOException {
            zip.putNextEntry(new ZipEntry(name));
            zip.write(data);
            zip.closeEntry();
        }
    }

    // ================================================================ MAIN
    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");

        Path docsDir = Path.of(args.length > 0 ? args[0] : "docs").toAbsolutePath().normalize();
        Files.createDirectories(docsDir);
        Path imgDir = docsDir.resolve("img");
        Files.createDirectories(imgDir);

        System.out.println("== Sinh sơ đồ ==");
        Diagrams.drawAll(imgDir);

        List<Block> blocks = buildBlocks();

        Path md = docsDir.resolve("SRS_Cinex_v1.0.md");
        Files.writeString(md, renderMarkdown(blocks), StandardCharsets.UTF_8);
        System.out.println("  [ok] " + md);

        Path html = docsDir.resolve("SRS_Cinex_v1.0.html");
        Files.writeString(html, renderHtml(blocks), StandardCharsets.UTF_8);
        System.out.println("  [ok] " + html);

        DocxWriter docx = new DocxWriter();
        for (Block blk : blocks) {
            if (blk instanceof Head h) {
                docx.head(h.level(), h.text());
            } else if (blk instanceof Paragraph p) {
                docx.paragraph(p.text());
            } else if (blk instanceof Table t) {
                docx.table(t.headers(), t.rows());
            } else if (blk instanceof Image im) {
                Path png = imgDir.resolve(im.file());
                try {
                    BufferedImage bi = ImageIO.read(png.toFile());
                    docx.image(png, bi.getWidth(), bi.getHeight(), im.caption());
                } catch (IOException e) {
                    throw new IllegalStateException("Không đọc được ảnh " + png, e);
                }
            } else if (blk instanceof PageBreak) {
                docx.pageBreak();
            }
        }
        Path docxPath = docsDir.resolve("SRS_Cinex_v1.0.docx");
        docx.write(docxPath);
        System.out.println("  [ok] " + docxPath);
    }

    // ============================================================== SƠ ĐỒ
    static final class Diagrams {

        static final Color BG = Color.WHITE;
        static final Color INK = new Color(0x1A, 0x1A, 0x1A);
        static final Color LINE = new Color(0x55, 0x55, 0x66);
        static final Color BRAND = new Color(0xB0, 0x00, 0x20);
        static final Color BLUE = new Color(0x1F, 0x38, 0x64);
        static final Color GREEN = new Color(0x1B, 0x6E, 0x3C);
        static final Color ORANGE = new Color(0xA5, 0x5A, 0x00);
        static final Color GREY = new Color(0x66, 0x66, 0x66);
        static final Color FILL = new Color(0xF4, 0xF4, 0xF8);
        static final Color FILL_BLUE = new Color(0xE8, 0xEF, 0xF9);
        static final Color FILL_GREEN = new Color(0xE9, 0xF5, 0xEC);
        static final Color FILL_ORANGE = new Color(0xFD, 0xF2, 0xE2);
        static final Color FILL_GREY = new Color(0xF0, 0xF0, 0xF0);

        static final String FONT = "SansSerif";

        record Rect(int x, int y, int w, int h) {
            int cx() { return x + w / 2; }
            int cy() { return y + h / 2; }
            int right() { return x + w; }
            int bottom() { return y + h; }
        }

        static BufferedImage img;
        static Graphics2D g;

        static void newCanvas(int w, int h) {
            img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.setColor(BG);
            g.fillRect(0, 0, w, h);
        }

        static void font(int size, boolean bold) {
            g.setFont(new Font(FONT, bold ? Font.BOLD : Font.PLAIN, size));
        }

        static void save(Path path) throws IOException {
            g.dispose();
            ImageIO.write(img, "png", path.toFile());
            g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        }

        // ------------------------------------------------------------ hộp
        static Rect box(int x, int y, int w, String title, List<String> lines,
                        Color header, Color fill, int fontSize) {
            font(fontSize, false);
            int lineH = fontSize + 5;
            int bodyH = lines.isEmpty() ? 8 : lines.size() * lineH + 6;
            int h = lineH + 8 + bodyH;
            g.setColor(fill);
            g.fill(new RoundRectangle2D.Float(x, y, w, h, 8, 8));
            g.setColor(header);
            g.setStroke(new BasicStroke(1.6f));
            g.draw(new RoundRectangle2D.Float(x, y, w, h, 8, 8));
            g.fill(new RoundRectangle2D.Float(x, y, w, lineH + 6, 8, 8));
            g.fillRect(x, y + lineH - 2, w, 8);
            g.setColor(Color.WHITE);
            font(fontSize, true);
            g.drawString(title, x + 8, y + lineH - 2);
            g.setColor(INK);
            font(fontSize - 1, false);
            int ty = y + lineH + 12;
            for (String line : lines) {
                g.drawString(line, x + 8, ty);
                ty += lineH;
            }
            return new Rect(x, y, w, h);
        }

        static Rect oval(int x, int y, int w, int h, String text, Color fill, int fontSize) {
            g.setColor(fill);
            g.fill(new Ellipse2D.Float(x, y, w, h));
            g.setColor(BLUE);
            g.setStroke(new BasicStroke(1.5f));
            g.draw(new Ellipse2D.Float(x, y, w, h));
            g.setColor(INK);
            font(fontSize, false);
            centerText(text, x + w / 2, y + h / 2, w - 16);
            return new Rect(x, y, w, h);
        }

        static Rect process(int x, int y, int w, int h, String text, Color fill, int fontSize) {
            g.setColor(fill);
            g.fill(new RoundRectangle2D.Float(x, y, w, h, 16, 16));
            g.setColor(LINE);
            g.setStroke(new BasicStroke(1.5f));
            g.draw(new RoundRectangle2D.Float(x, y, w, h, 16, 16));
            g.setColor(INK);
            font(fontSize, false);
            centerText(text, x + w / 2, y + h / 2, w - 18);
            return new Rect(x, y, w, h);
        }

        static Rect decision(int x, int y, int w, int h, String text, int fontSize) {
            Path2D path = new Path2D.Float();
            path.moveTo(x + w / 2, y);
            path.lineTo(x + w, y + h / 2);
            path.lineTo(x + w / 2, y + h);
            path.lineTo(x, y + h / 2);
            path.closePath();
            g.setColor(FILL_ORANGE);
            g.fill(path);
            g.setColor(ORANGE);
            g.setStroke(new BasicStroke(1.6f));
            g.draw(path);
            g.setColor(INK);
            font(fontSize, false);
            centerText(text, x + w / 2, y + h / 2, (int) (w * 0.62));
            return new Rect(x, y, w, h);
        }

        static Rect terminator(int x, int y, int w, int h, String text, Color fill, int fontSize) {
            g.setColor(fill);
            g.fill(new RoundRectangle2D.Float(x, y, w, h, h / 2.0f, h / 2.0f));
            g.setColor(GREEN);
            g.setStroke(new BasicStroke(1.6f));
            g.draw(new RoundRectangle2D.Float(x, y, w, h, h / 2.0f, h / 2.0f));
            g.setColor(INK);
            font(fontSize, true);
            centerText(text, x + w / 2, y + h / 2, w - 20);
            return new Rect(x, y, w, h);
        }

        static void centerText(String text, int cx, int cy, int maxWidth) {
            List<String> lines = wrap(text, g.getFontMetrics(), maxWidth);
            int lh = g.getFontMetrics().getHeight();
            int y0 = cy - (lines.size() - 1) * lh / 2 + g.getFontMetrics().getAscent() / 2 - 2;
            for (int i = 0; i < lines.size(); i++) {
                String l = lines.get(i);
                int w = g.getFontMetrics().stringWidth(l);
                g.drawString(l, cx - w / 2, y0 + i * lh);
            }
        }

        static List<String> wrap(String text, java.awt.FontMetrics fm, int maxWidth) {
            List<String> out = new ArrayList<>();
            for (String para : text.split("\n")) {
                StringBuilder line = new StringBuilder();
                for (String word : para.split(" ")) {
                    String candidate = line.isEmpty() ? word : line + " " + word;
                    if (fm.stringWidth(candidate) <= maxWidth || line.isEmpty()) {
                        line = new StringBuilder(candidate);
                    } else {
                        out.add(line.toString());
                        line = new StringBuilder(word);
                    }
                }
                out.add(line.toString());
            }
            return out;
        }

        static void line(int x1, int y1, int x2, int y2, boolean arrow, Color color) {
            g.setColor(color == null ? LINE : color);
            g.setStroke(new BasicStroke(1.5f));
            g.draw(new Line2D.Float(x1, y1, x2, y2));
            if (arrow) {
                arrowHead(x1, y1, x2, y2);
            }
        }

        static void arrowHead(int x1, int y1, int x2, int y2) {
            double angle = Math.atan2(y2 - y1, x2 - x1);
            int size = 9;
            g.setColor(LINE);
            Path2D path = new Path2D.Float();
            path.moveTo(x2, y2);
            path.lineTo(x2 - size * Math.cos(angle - Math.PI / 6), y2 - size * Math.sin(angle - Math.PI / 6));
            path.lineTo(x2 - size * Math.cos(angle + Math.PI / 6), y2 - size * Math.sin(angle + Math.PI / 6));
            path.closePath();
            g.fill(path);
        }

        static void label(int x, int y, String text, Color color) {
            font(11, false);
            int w = g.getFontMetrics().stringWidth(text) + 8;
            g.setColor(new Color(255, 255, 255, 235));
            g.fillRect(x - 4, y - 12, w, 15);
            g.setColor(color == null ? GREY : color);
            g.drawString(text, x, y);
        }

        /** Đường gấp khúc nối hai hộp. */
        static void connect(Rect from, Rect to, String text) {
            int sx = from.right();
            int sy = from.y + 16;
            int tx = to.x;
            int ty = to.y + 16;
            int midX = (sx + tx) / 2;
            g.setColor(LINE);
            g.setStroke(new BasicStroke(1.4f));
            Path2D path = new Path2D.Float();
            path.moveTo(sx, sy);
            path.lineTo(midX, sy);
            path.lineTo(midX, ty);
            path.lineTo(tx, ty);
            g.draw(path);
            arrowHead(midX, ty, tx, ty);
            if (text != null) {
                label(midX + 4, (sy + ty) / 2, text, GREY);
            }
        }

        /** Đường gấp khúc đi vòng (dùng khi hộp đích nằm bên trái). */
        static void connectBack(Rect from, Rect to, String text) {
            int sx = from.x + from.w / 2;
            int sy = from.bottom();
            int tx = to.x + to.w / 2;
            int ty = to.bottom();
            int down = Math.max(sy, ty) + 34;
            g.setColor(LINE);
            g.setStroke(new BasicStroke(1.4f));
            Path2D path = new Path2D.Float();
            path.moveTo(sx, sy);
            path.lineTo(sx, down);
            path.lineTo(tx, down);
            path.lineTo(tx, ty);
            g.draw(path);
            arrowHead(tx, ty, tx, ty + 1);
            if (text != null) {
                label(Math.min(sx, tx) + 6, down - 4, text, GREY);
            }
        }

        static void title(String text, String sub, int w) {
            font(20, true);
            g.setColor(BRAND);
            int tw = g.getFontMetrics().stringWidth(text);
            g.drawString(text, (w - tw) / 2, 36);
            if (sub != null) {
                font(13, false);
                g.setColor(GREY);
                int sw = g.getFontMetrics().stringWidth(sub);
                g.drawString(sub, (w - sw) / 2, 58);
            }
        }

        // --------------------------------------------------------- các sơ đồ
        static void drawAll(Path imgDir) throws IOException {
            drawErd(imgDir.resolve("erd.png"));
            drawUseCase(imgDir.resolve("usecase.png"));
            drawFlow(imgDir.resolve("flow.png"));
            drawState(imgDir.resolve("state.png"));
            drawSiteMap(imgDir.resolve("sitemap.png"));
        }

        // ---- ERD
        /** Chiều cao ước tính của một hộp, phải khớp với hàm box(). */
        static int boxHeight(int nLines) {
            int lineH = 12 + 5;
            return lineH + 8 + (nLines == 0 ? 8 : nLines * lineH + 6);
        }

        /** Đường nối trong cùng một cột: đi vòng bên phải hộp. */
        static void connectSide(Rect a, Rect b, String text) {
            int gx = a.right() + 14;
            g.setColor(LINE);
            g.setStroke(new BasicStroke(1.4f));
            Path2D path = new Path2D.Float();
            path.moveTo(a.right(), a.y + 18);
            path.lineTo(gx, a.y + 18);
            path.lineTo(gx, b.y + 18);
            path.lineTo(b.right(), b.y + 18);
            g.draw(path);
            arrowHead(b.right(), b.y + 18, b.right() - 1, b.y + 18);
            if (text != null) {
                label(gx + 4, (a.y + b.y) / 2 + 20, text, GREY);
            }
        }

        /** Đường nối xa: chạy dọc theo đường bus ở dưới các hộp rồi đi lên từ bên phải. */
        static void connectBus(Rect a, Rect b, int level, String text) {
            int gx = a.x - 14;
            int rx = b.right() + 16;
            g.setColor(LINE);
            g.setStroke(new BasicStroke(1.4f));
            Path2D path = new Path2D.Float();
            path.moveTo(a.x, a.y + 18);
            path.lineTo(gx, a.y + 18);
            path.lineTo(gx, level);
            path.lineTo(rx, level);
            path.lineTo(rx, b.y);
            path.lineTo(b.right(), b.y);
            g.draw(path);
            arrowHead(b.right(), b.y, b.right() - 1, b.y);
            if (text != null) {
                label((gx + rx) / 2, level - 6, text, GREY);
            }
        }

        static void drawErd(Path path) throws IOException {
            final int COLW = 350;
            final int X0 = 40, X1 = 470, X2 = 900, X3 = 1330;
            final int Y0 = 95;
            final int GAP = 30;

            // Khai báo nội dung các cột
            String[][] col0 = {
                    {"Role", "id", "name", "description"},
                    {"User", "id", "name", "email", "password", "phone", "role_id", "status"},
                    {"Province", "id", "code", "name", "region", "latitude", "longitude"},
            };
            String[][] col1 = {
                    {"Cinema", "id", "province_id", "name", "address", "city", "region", "brand"},
                    {"Screen", "id", "cinema_id", "name", "screen_type", "rows_count", "cols_count"},
                    {"Seat", "id", "screen_id", "row_label", "seat_number", "seat_type"},
            };
            String[][] col2 = {
                    {"Movie", "id", "tmdb_id", "title", "genre", "duration", "rating", "poster",
                            "ticket_price", "vip_price", "release_date", "status"},
                    {"Showtime", "id", "movie_id", "cinema_id", "screen_id", "start_time", "end_time",
                            "base_price", "vip_price", "status"},
            };
            String[][] col3 = {
                    {"Booking", "id", "code", "user_id", "showtime_id", "total_amount", "discount_amount",
                            "final_amount", "promotion_code", "payment_method", "status"},
                    {"BookingSeat", "id", "booking_id", "showtime_id", "seat_id", "seat_label", "price", "status"},
                    {"Ticket", "id", "ticket_code", "booking_id", "user_id", "showtime_id", "seat_label",
                            "qr_code", "status"},
                    {"Bill", "id", "code", "booking_id", "user_id", "subtotal", "discount", "total"},
                    {"Payment", "id", "code", "booking_id", "user_id", "provider", "amount", "status", "paid_at"},
                    {"Promotion", "id", "code", "title", "discount_type", "discount_value", "max_discount",
                            "min_amount", "end_date", "is_active"},
            };

            Color[] accents = {BRAND, BLUE, GREEN, ORANGE};
            Color[] fills = {FILL, FILL_BLUE, FILL_GREEN, FILL_ORANGE};

            // Tính vị trí và chiều cao từng cột để biết trước kích thước canvas
            int[] colX = {X0, X1, X2, X3};
            String[][][] cols = {col0, col1, col2, col3};
            int maxBottom = Y0;
            for (int c = 0; c < cols.length; c++) {
                int y = Y0;
                for (String[] spec : cols[c]) {
                    y += boxHeight(spec.length - 1) + GAP;
                    maxBottom = Math.max(maxBottom, y - GAP);
                }
            }
            int bus1 = maxBottom + 46;
            int bus2 = maxBottom + 92;
            int W = X3 + COLW + 60;
            int H = bus2 + 90;

            newCanvas(W, H);
            title("Sơ đồ ERD – Hệ thống đặt vé xem phim CineX",
                    "14 thực thể · quan hệ 1:N và N:N thông qua bảng trung gian", W);

            // Vẽ các hộp
            java.util.Map<String, Rect> boxes = new java.util.LinkedHashMap<>();
            for (int c = 0; c < cols.length; c++) {
                int y = Y0;
                for (String[] spec : cols[c]) {
                    List<String> lines = new ArrayList<>();
                    for (int i = 1; i < spec.length; i++) {
                        lines.add(spec[i]);
                    }
                    Rect r = box(colX[c], y, COLW, spec[0], lines, accents[c], fills[c], 12);
                    boxes.put(spec[0], r);
                    y += boxHeight(spec.length - 1) + GAP;
                }
            }

            // Quan hệ trong cùng cột
            connectSide(boxes.get("Role"), boxes.get("User"), "1:N");
            connectSide(boxes.get("Cinema"), boxes.get("Screen"), "1:N");
            connectSide(boxes.get("Screen"), boxes.get("Seat"), "1:N");
            connectSide(boxes.get("Movie"), boxes.get("Showtime"), "1:N");
            connectSide(boxes.get("Booking"), boxes.get("BookingSeat"), "1:N");
            connectSide(boxes.get("BookingSeat"), boxes.get("Ticket"), "1:N");
            connectSide(boxes.get("Booking"), boxes.get("Bill"), "1:1");
            connectSide(boxes.get("Booking"), boxes.get("Payment"), "1:N");

            // Quan hệ giữa hai cột liền nhau (đi qua rãnh giữa các cột)
            connect(boxes.get("Province"), boxes.get("Cinema"), "1:N");
            connect(boxes.get("Cinema"), boxes.get("Showtime"), "1:N");
            connect(boxes.get("Screen"), boxes.get("Showtime"), "1:N");
            connect(boxes.get("Showtime"), boxes.get("Booking"), "1:N");

            // Quan hệ xa: đi qua đường bus dưới các hộp
            connectBus(boxes.get("User"), boxes.get("Booking"), bus1, "User 1 — N Booking");
            connectBus(boxes.get("Seat"), boxes.get("BookingSeat"), bus2, "Seat N — N BookingSeat");

            font(12, true);
            g.setColor(GREY);
            g.drawString("Chú thích: quan hệ đi từ khóa chính (1) sang khóa ngoại (N). "
                    + "Bảng booking_seats có ràng buộc UNIQUE (showtime_id, seat_id) chống bán trùng ghế.",
                    40, H - 26);

            save(path);
        }

        // ---- USE CASE
        static void drawUseCase(Path path) throws IOException {
            int W = 1500, H = 1020;
            newCanvas(W, H);
            title("Sơ đồ Use Case – Hệ thống đặt vé xem phim CineX",
                    "Tác nhân: Người dùng · Nhân viên rạp (mở rộng) · Admin", W);

            // Ranh giới hệ thống
            int bx = 250, by = 100, bw = 1010, bh = 830;
            g.setColor(new Color(0xFA, 0xFA, 0xFC));
            g.fill(new RoundRectangle2D.Float(bx, by, bw, bh, 18, 18));
            g.setColor(BLUE);
            g.setStroke(new BasicStroke(2f));
            g.draw(new RoundRectangle2D.Float(bx, by, bw, bh, 18, 18));
            font(13, true);
            g.setColor(BLUE);
            g.drawString("Hệ thống đặt vé xem phim CineX", bx + 18, by + 24);

            String[][] ucs = {
                    {"UC1", "Đăng ký / Đăng nhập"}, {"UC2", "Xem danh sách phim"},
                    {"UC3", "Chi tiết phim & lịch chiếu"}, {"UC4", "Xem hệ thống rạp"},
                    {"UC5", "Xem lịch chiếu theo ngày"}, {"UC6", "Chọn ghế"},
                    {"UC7", "Giữ ghế & tạo đơn"}, {"UC8", "Khuyến mãi & thanh toán"},
                    {"UC9", "Vé của tôi & huỷ đơn"}, {"UC10", "Hồ sơ & đổi mật khẩu"},
                    {"UC11", "Xem khuyến mãi"}, {"UC12", "Bảng điều khiển & doanh thu"},
                    {"UC13", "Quản lý phim"}, {"UC14", "Quản lý hệ thống rạp"},
                    {"UC15", "Quản lý đơn đặt vé"}, {"UC16", "Quản lý khuyến mãi"},
            };

            int colX1 = bx + 70, colX2 = bx + 560;
            int rowY = by + 70, rowH = 88, ovalW = 400, ovalH = 58;

            List<Rect> shapes = new ArrayList<>();
            for (int i = 0; i < ucs.length; i++) {
                int cx = (i % 2 == 0) ? colX1 : colX2;
                int cy = rowY + (i / 2) * rowH;
                shapes.add(oval(cx, cy, ovalW, ovalH, ucs[i][0] + " – " + ucs[i][1], FILL_BLUE, 12));
            }

            // Tác nhân
            Rect actorUser = actor(80, 300, "Người dùng");
            Rect actorStaff = actor(80, 700, "Nhân viên rạp");
            Rect actorAdmin = actor(1350, 300, "Admin");

            // Đường nối tác nhân
            int[] userUcs = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            for (int i : userUcs) {
                link(actorUser, shapes.get(i));
            }
            int[] staffUcs = {8, 12, 13, 14, 15};
            for (int i : staffUcs) {
                link(actorStaff, shapes.get(i));
            }
            int[] adminUcs = {11, 12, 13, 14, 15};
            for (int i : adminUcs) {
                link(actorAdmin, shapes.get(i));
            }

            font(12, true);
            g.setColor(GREY);
            g.drawString("UC1–UC16: đã triển khai trong khung Spring Boot hiện tại.", bx + 18, by + bh - 14);
            g.drawString("TMDB và PayOS đã có luồng; UC17–UC20 là các chức năng mở rộng chưa triển khai.",
                    bx + 18, by + bh - 30);

            save(path);
        }

        static Rect actor(int x, int y, String label) {
            g.setColor(INK);
            g.setStroke(new BasicStroke(2f));
            g.draw(new Ellipse2D.Float(x + 22, y, 22, 22));
            g.draw(new Line2D.Float(x + 33, y + 22, x + 33, y + 56));
            g.draw(new Line2D.Float(x + 10, y + 38, x + 56, y + 38));
            g.draw(new Line2D.Float(x + 33, y + 56, x + 14, y + 84));
            g.draw(new Line2D.Float(x + 33, y + 56, x + 52, y + 84));
            font(12, true);
            int w = g.getFontMetrics().stringWidth(label);
            g.drawString(label, x + 33 - w / 2, y + 102);
            return new Rect(x, y, 66, 104);
        }

        static void link(Rect a, Rect b) {
            int x1 = a.right(), y1 = a.cy();
            int x2 = b.x, y2 = b.cy();
            g.setColor(new Color(0x99, 0x99, 0xAA));
            g.setStroke(new BasicStroke(1.1f));
            Path2D path = new Path2D.Float();
            path.moveTo(x1, y1);
            int mid = (x1 + x2) / 2;
            path.lineTo(mid, y1);
            path.lineTo(mid, y2);
            path.lineTo(x2, y2);
            g.draw(path);
        }

        // ---- FLOW
        static void drawFlow(Path path) throws IOException {
            int bw = 480, bh = 56;
            int cx = 540;

            // Các bước: {nhãn, loại} với loại: 0 = hộp, 1 = quyết định, 2 = kết thúc
            String[][] steps = {
                    {"Bắt đầu", "2"},
                    {"Chọn phim → rạp → ngày → suất chiếu", "0"},
                    {"Hệ thống dựng sơ đồ ghế của phòng", "0"},
                    {"Người dùng chọn ghế (giao diện tự tính tổng tiền)", "0"},
                    {"Còn ghế trống?", "1"},
                    {"Gửi danh sách ghế lên server", "0"},
                    {"Suất chiếu còn hiệu lực\nvà ghế hợp lệ?", "1"},
                    {"Tạo Booking (pending) + giữ ghế 15 phút", "0"},
                    {"Nhập mã khuyến mãi + chọn phương thức thanh toán", "0"},
                    {"Mã khuyến mãi hợp lệ?", "1"},
                    {"Server tính lại giá, áp khuyến mãi, chuyển confirmed", "0"},
                    {"Phát hành vé điện tử + tạo hoá đơn", "0"},
                    {"Hiển thị vé trong “Vé của tôi”", "2"},
            };

            int[] stepH = new int[steps.length];
            for (int i = 0; i < steps.length; i++) {
                stepH[i] = steps[i][1].equals("1") ? 88 : bh;
            }

            int gap = 40;
            int y = 100;
            int[] stepY = new int[steps.length];
            int bottom = y;
            for (int i = 0; i < steps.length; i++) {
                stepY[i] = y;
                y += stepH[i] + gap;
                bottom = Math.max(bottom, stepY[i] + stepH[i]);
            }
            // Khối kết thúc báo lỗi nằm bên trái
            int errY = bottom + 60;
            int W = 1180;
            int H = errY + 130;

            newCanvas(W, H);
            title("Sơ đồ luồng – Đặt vé qua giao diện (UC6 → UC7 → UC8)",
                    "Luồng dự phòng khi không dùng trợ lý AI hội thoại", W);

            Rect[] shapes = new Rect[steps.length];
            for (int i = 0; i < steps.length; i++) {
                int h = stepH[i];
                int x = cx - bw / 2;
                if (steps[i][1].equals("1")) {
                    shapes[i] = decision(x, stepY[i], bw, h, steps[i][0], 13);
                } else if (steps[i][1].equals("2")) {
                    shapes[i] = terminator(x, stepY[i], bw, h, steps[i][0], FILL_GREEN, 14);
                } else {
                    shapes[i] = process(x, stepY[i], bw, h, steps[i][0], FILL_BLUE, 13);
                }
            }

            for (int i = 0; i < steps.length - 1; i++) {
                line(cx, shapes[i].bottom(), cx, shapes[i + 1].y(), true, LINE);
            }

            // Nhánh "không" của Còn ghế trống → quay lại bước chọn ghế
            Rect r5 = shapes[4], r4 = shapes[3];
            int ex = cx + 330;
            line(cx + bw / 2, r5.cy(), ex, r5.cy(), true, BRAND);
            line(ex, r5.cy(), ex, r4.cy(), true, BRAND);
            line(ex, r4.cy(), r4.right(), r4.cy(), true, BRAND);
            label(ex + 8, r5.cy() - 6, "Không – chọn ghế khác", BRAND);

            // Nhánh "không" của Suất chiếu còn hiệu lực → khối báo lỗi
            Rect r7 = shapes[6];
            int ex2 = 250;
            Rect errBox = terminator(25, errY, 200, bh, "Báo lỗi", FILL_GREY, 13);
            line(cx - bw / 2, r7.cy(), ex2, r7.cy(), true, BRAND);
            line(ex2, r7.cy(), ex2, errBox.cy(), true, BRAND);
            line(ex2, errBox.cy(), errBox.right(), errBox.cy(), true, BRAND);
            label(ex2 + 8, r7.cy() - 6, "Không", BRAND);

            // Nhánh "không" của Mã khuyến mãi → quay lại bước nhập mã
            Rect r10 = shapes[9], r9 = shapes[8];
            int ex3 = cx + 330;
            line(cx + bw / 2, r10.cy(), ex3, r10.cy(), true, ORANGE);
            line(ex3, r10.cy(), ex3, r9.cy(), true, ORANGE);
            line(ex3, r9.cy(), r9.right(), r9.cy(), true, ORANGE);
            label(ex3 + 8, r10.cy() - 6, "Không – thanh toán giá gốc", ORANGE);

            font(12, false);
            g.setColor(GREY);
            g.drawString("Mọi bước kiểm tra nghiệp vụ và tính giá đều thực hiện phía server; "
                    + "giao diện chỉ hiển thị và gửi yêu cầu.", 40, H - 26);

            save(path);
        }

        // ---- STATE
        static void drawState(Path path) throws IOException {
            int W = 1300, H = 640;
            newCanvas(W, H);
            title("Sơ đồ chuyển trạng thái – Đơn đặt vé (Booking)",
                    "Vòng đời từ lúc giữ ghế đến khi hoàn tất hoặc bị huỷ", W);

            Rect pending = state(110, 250, "Pending", "Ghế được giữ tạm (15 phút)", FILL_ORANGE, ORANGE);
            Rect confirmed = state(500, 120, "Confirmed", "Đã thanh toán, phát hành vé + hoá đơn", FILL_GREEN, GREEN);
            Rect cancelled = state(500, 400, "Cancelled", "Đã huỷ, ghế được trả lại", FILL_GREY, GREY);
            Rect expired = state(940, 400, "Expired", "Hết giờ giữ ghế, tự trả ghế", FILL_GREY, GREY);

            // Pending -> Confirmed (đi lên trên)
            g.setColor(GREEN);
            g.setStroke(new BasicStroke(2f));
            Path2D p1 = new Path2D.Float();
            p1.moveTo(pending.cx(), pending.y);
            p1.lineTo(pending.cx(), confirmed.cy());
            p1.lineTo(confirmed.x, confirmed.cy());
            g.draw(p1);
            arrowHead(confirmed.x, confirmed.cy(), confirmed.x - 1, confirmed.cy());
            label(pending.cx() + 12, confirmed.cy() - 12, "Thanh toán thành công", GREEN);

            // Pending -> Cancelled (đi xuống rồi sang phải)
            g.setColor(GREY);
            g.setStroke(new BasicStroke(2f));
            Path2D p2 = new Path2D.Float();
            p2.moveTo(pending.cx(), pending.bottom());
            p2.lineTo(pending.cx(), cancelled.cy());
            p2.lineTo(cancelled.x, cancelled.cy());
            g.draw(p2);
            arrowHead(cancelled.x, cancelled.cy(), cancelled.x - 1, cancelled.cy());
            label(pending.cx() + 12, cancelled.cy() + 22, "Người dùng huỷ", GREY);

            // Confirmed -> Cancelled (đi thẳng xuống)
            g.setColor(BRAND);
            g.setStroke(new BasicStroke(2f));
            Path2D p3 = new Path2D.Float();
            p3.moveTo(confirmed.cx(), confirmed.bottom());
            p3.lineTo(confirmed.cx(), cancelled.y);
            g.draw(p3);
            arrowHead(confirmed.cx(), cancelled.y, confirmed.cx(), cancelled.y - 1);
            label(confirmed.cx() + 12, cancelled.y - 16, "Huỷ trước giờ chiếu", BRAND);

            // Pending -> Expired (đi vòng dưới các hộp)
            g.setColor(ORANGE);
            g.setStroke(new BasicStroke(2f));
            Path2D p4 = new Path2D.Float();
            p4.moveTo(pending.cx(), pending.bottom());
            p4.lineTo(pending.cx(), expired.bottom() + 52);
            p4.lineTo(expired.cx(), expired.bottom() + 52);
            p4.lineTo(expired.cx(), expired.bottom());
            g.draw(p4);
            arrowHead(expired.cx(), expired.bottom(), expired.cx(), expired.bottom() - 1);
            label(pending.cx() + 20, expired.bottom() + 44,
                    "Job định kỳ giải phóng ghế khi hết 15 phút", ORANGE);

            font(12, false);
            g.setColor(GREY);
            g.drawString("Trạng thái được lưu ở cột `status` của bảng bookings. "
                    + "Mỗi lần chuyển trạng thái được ghi kèm mã đơn để đối soát.", 60, H - 26);

            save(path);
        }

        static Rect state(int x, int y, String name, String desc, Color fill, Color border) {
            int w = 260, h = 86;
            g.setColor(fill);
            g.fill(new RoundRectangle2D.Float(x, y, w, h, 42, 42));
            g.setColor(border);
            g.setStroke(new BasicStroke(2f));
            g.draw(new RoundRectangle2D.Float(x, y, w, h, 42, 42));
            font(15, true);
            g.setColor(INK);
            int tw = g.getFontMetrics().stringWidth(name);
            g.drawString(name, x + (w - tw) / 2, y + 32);
            font(11, false);
            g.setColor(GREY);
            List<String> lines = wrap(desc, g.getFontMetrics(), w - 24);
            int ly = y + 52;
            for (String l : lines) {
                int lw = g.getFontMetrics().stringWidth(l);
                g.drawString(l, x + (w - lw) / 2, ly);
                ly += 14;
            }
            return new Rect(x, y, w, h);
        }

        // ---- SITEMAP
        static void drawSiteMap(Path path) throws IOException {
            int W = 1440, H = 900;
            newCanvas(W, H);
            title("Site Map – Hệ thống đặt vé xem phim CineX",
                    "Cấu trúc trang web và khu vực quản trị", W);

            int rootW = 300, rootH = 52;
            Rect root = node(570, 80, rootW, rootH, "CineX – Trang chủ", BRAND, Color.WHITE, 13);

            String[][] groups = {
                    {"Khám phá", "Trang chủ", "Danh sách phim", "Chi tiết phim", "Lịch chiếu", "Hệ thống rạp", "Khuyến mãi"},
                    {"Đặt vé", "Chọn ghế", "Giữ ghế 15 phút", "Thanh toán", "Vé điện tử"},
                    {"Tài khoản", "Đăng ký / Đăng nhập", "Hồ sơ", "Đổi mật khẩu", "Vé của tôi"},
                    {"Quản trị", "Bảng điều khiển", "Quản lý phim", "Quản lý rạp", "Quản lý đơn đặt vé", "Quản lý khuyến mãi"},
            };
            int[] gx = {60, 420, 760, 1060};
            List<List<Rect>> all = new ArrayList<>();
            for (int i = 0; i < groups.length; i++) {
                List<Rect> nodes = new ArrayList<>();
                int y = 200;
                Rect head = node(gx[i], y, 280, 44, groups[i][0], BLUE, Color.WHITE, 13);
                nodes.add(head);
                y += 76;
                for (int k = 1; k < groups[i].length; k++) {
                    Rect n = node(gx[i], y, 280, 40, groups[i][k], FILL_BLUE, BLUE, 12);
                    nodes.add(n);
                    y += 56;
                }
                all.add(nodes);
                // root -> group header
                line(root.cx(), root.bottom(), root.cx(), 166, false, LINE);
                Path2D p = new Path2D.Float();
                p.moveTo(root.cx(), 166);
                p.lineTo(head.cx(), 166);
                p.lineTo(head.cx(), head.y());
                g.setColor(LINE);
                g.setStroke(new BasicStroke(1.6f));
                g.draw(p);
                arrowHead(head.cx(), head.y(), head.cx(), head.y() + 1);
            }

            font(12, false);
            g.setColor(GREY);
            g.drawString("Mỗi nhánh là một nhóm chức năng; các khối bên dưới là các trang thực thi tương ứng trong hệ thống.",
                    60, H - 26);

            save(path);
        }

        static Rect node(int x, int y, int w, int h, String text, Color fill, Color border, int fontSize) {
            g.setColor(fill);
            g.fill(new RoundRectangle2D.Float(x, y, w, h, 10, 10));
            g.setColor(border);
            g.setStroke(new BasicStroke(1.6f));
            g.draw(new RoundRectangle2D.Float(x, y, w, h, 10, 10));
            font(fontSize, true);
            g.setColor(border == Color.WHITE ? Color.WHITE : BLUE);
            centerText(text, x + w / 2, y + h / 2, w - 16);
            return new Rect(x, y, w, h);
        }
    }
}
