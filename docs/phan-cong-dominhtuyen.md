# Phân công công việc - Đỗ Minh Tuyến

Trích từ **docs/Bao_Cao_Du_An_CineX_cap_nhat.docx** (Nhóm 03 - lớp HN-KS24-CNTT1). File này nằm trên nhánh `dominhtuyen-Database-&-Data-Lead`.

| Thông tin | Nội dung |
| --- | --- |
| Họ và tên | Đỗ Minh Tuyến |
| Vai trò | Database & Data Lead |
| Nhánh Git | `dominhtuyen-Database-&-Data-Lead` |

## Phạm vi trách nhiệm chi tiết

- Thiết kế sơ đồ quan hệ CSDL (ERD) và chuẩn hóa dữ liệu.
- Viết kịch bản khởi tạo CSDL (SQL scripts, Seeders) và cấu hình đánh chỉ mục (Index).
- Xây dựng danh sách kịch bản kiểm thử (Test Cases) cho toàn bộ ứng dụng.
- Thực hiện kiểm thử chức năng (Functional Testing) và kiểm thử luồng người dùng E2E.
- Phối hợp với Frontend/Backend để phát hiện, theo dõi và xác nhận sửa lỗi (Bug Tracking).

## Bảng phân công theo chức năng (F01-F36)

| Mã | Chức năng | Đầu việc phụ trách |
| --- | --- | --- |
| F01 | Đăng ký tài khoản | Thiết kế dữ liệu User/Role; lập test trùng email, phone và dữ liệu thiếu. |
| F02 | Đăng nhập và đăng xuất | Kiểm tra dữ liệu tài khoản mẫu; test thông tin đúng/sai và trạng thái khóa. |
| F03 | Phân quyền truy cập | Lập ma trận quyền và test truy cập trái phép từng khu vực. |
| F04 | Cập nhật hồ sơ | Kiểm tra ràng buộc dữ liệu hồ sơ; test cập nhật hợp lệ/trùng. |
| F05 | Đổi mật khẩu | Test mật khẩu cũ sai, mật khẩu mới thiếu độ dài và đổi thành công. |
| F06 | Xem trang chủ | Chuẩn bị dữ liệu seed và test trường hợp danh sách rỗng. |
| F07 | Danh sách và tìm kiếm phim | Rà soát dữ liệu phim; test từ khóa, trang đầu/cuối và không có kết quả. |
| F08 | Chi tiết phim và trailer | Kiểm tra dữ liệu metadata; test thiếu poster/trailer/thông tin tùy chọn. |
| F09 | Tra cứu hệ thống rạp | Chuẩn hóa dữ liệu tỉnh và rạp; test rạp thiếu địa chỉ/tỉnh. |
| F10 | Lịch chiếu tại rạp | Rà soát liên kết rạp/phòng/suất; test rạp không có suất. |
| F11 | Lịch chiếu theo ngày | Test biên ngày, ngày không có suất và bộ lọc rạp. |
| F12 | Chọn suất chiếu | Kiểm thử suất hết hạn, bắt đầu chiếu và liên kết phòng. |
| F13 | Xem sơ đồ ghế | Kiểm tra cấu hình ghế/phòng; test sơ đồ thiếu ghế và trạng thái ghế. |
| F14 | Chọn và bỏ chọn ghế | Test loại ghế, ghế không hoạt động và giá thường/VIP. |
| F15 | Giữ ghế và tạo đơn | Kiểm thử hai yêu cầu cùng ghế, dữ liệu đơn và trạng thái ban đầu. |
| F16 | Giải phóng ghế hết hạn | Test thời điểm hết hạn, ghế được mở lại và tác vụ chạy định kỳ. |
| F17 | Xem và áp dụng khuyến mãi | Kiểm tra dữ liệu promotion; test hợp lệ, hết hạn, vượt lượt, không đạt tối thiểu. |
| F18 | Thanh toán đơn | Test số tiền, khuyến mãi, trạng thái thành công/thất bại. |
| F19 | Thanh toán PayOS | Chuẩn bị dữ liệu payment; kiểm thử webhook hợp lệ, sai chữ ký và gọi lặp. |
| F20 | Phát hành vé điện tử | Kiểm tra quan hệ vé/ghế/đơn; test số vé khớp ghế đã mua. |
| F21 | Phát hành hóa đơn | Kiểm tra tổng tiền/giảm giá; test mỗi đơn phát hành hóa đơn đúng quy tắc. |
| F22 | Lịch sử đặt và vé của tôi | Test cách ly dữ liệu giữa hai người dùng và trạng thái rỗng. |
| F23 | Hủy đơn đặt vé | Test quyền sở hữu, trạng thái đơn và việc giải phóng ghế. |
| F24 | Bảng điều khiển quản trị | Đối chiếu số liệu với dữ liệu nguồn; test hệ thống chưa có giao dịch. |
| F25 | Quản lý phim | Kiểm tra dữ liệu/seed và test validation trường phim. |
| F26 | Quản lý rạp, phòng, ghế | Thiết kế/kiểm tra quan hệ dữ liệu và ràng buộc xóa/sửa. |
| F27 | Quản lý suất chiếu | Test giờ bắt đầu/kết thúc, phòng trùng lịch và liên kết phim. |
| F28 | Quản lý đơn đặt vé | Test phân trang, dữ liệu khách/đơn và trạng thái. |
| F29 | Quản lý khuyến mãi | Test mã duy nhất, mức giảm, hạn và giới hạn lượt. |
| F30 | Quản lý người dùng | Test quyền, trạng thái khóa và dữ liệu người dùng. |
| F31 | Báo cáo doanh thu | Đối chiếu kết quả truy vấn với giao dịch mẫu và quy tắc loại đơn. |
| F32 | Đồng bộ TMDB/trailer | Kiểm thử cập nhật không tạo trùng, khóa thiếu và lỗi dịch vụ ngoài. |
| F33 | Trợ lý AI tư vấn | Xây test kịch bản tư vấn và lỗi dịch vụ; tính năng mở rộng. |
| F34 | JWT, OTP hoặc OAuth | Kiểm thử token, OTP, lỗi nhà cung cấp; tính năng mở rộng. |
| F35 | Bán combo bắp nước | Thiết kế dữ liệu combo/tồn kho; test cập nhật tồn và tổng đơn. |
| F36 | Check-in vé QR | Kiểm thử QR hợp lệ, sai suất, đã dùng và không tồn tại; tính năng mở rộng. |

## Đầu ra phụ trách

- ERD/schema/seed; test case, functional/E2E testing và theo dõi lỗi.

