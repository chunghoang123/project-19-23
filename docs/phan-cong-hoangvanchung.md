# Phân công công việc - Hoàng Văn Chung

Trích từ **docs/Bao_Cao_Du_An_CineX_cap_nhat.docx** (Nhóm 03 - lớp HN-KS24-CNTT1). File này nằm trên nhánh `hoangvanchung-Backend-&-DevOps-Lead`.

| Thông tin | Nội dung |
| --- | --- |
| Họ và tên | Hoàng Văn Chung |
| Vai trò | Backend & DevOps Lead |
| Nhánh Git | `hoangvanchung-Backend-&-DevOps-Lead` |

## Phạm vi trách nhiệm chi tiết

- Thiết kế kiến trúc hệ thống, xây dựng toàn bộ hệ thống RESTful API.
- Phát triển logic nghiệp vụ cốt lõi (Đặt vé, phim, suất chiếu, tính tiền...).
- Xây dựng hệ thống bảo mật & phân quyền Auth (Spring Security, JWT/Session).
- Tích hợp cổng thanh toán (PayOS) và dịch vụ bên thứ ba (TMDB API).
- Đóng gói ứng dụng với Docker/Docker Compose và cấu hình môi trường/triển khai.

## Bảng phân công theo chức năng (F01-F36)

| Mã | Chức năng | Đầu việc phụ trách |
| --- | --- | --- |
| F01 | Đăng ký tài khoản | Xử lý đăng ký, kiểm tra email/số điện thoại trùng và băm mật khẩu. |
| F02 | Đăng nhập và đăng xuất | Cấu hình xác thực session, điều hướng theo vai trò và kết thúc phiên. |
| F03 | Phân quyền truy cập | Khai báo bảo vệ route và kiểm soát quyền user/admin/staff. |
| F04 | Cập nhật hồ sơ | Cung cấp nghiệp vụ cập nhật hồ sơ và kiểm tra số điện thoại duy nhất. |
| F05 | Đổi mật khẩu | Kiểm tra mật khẩu cũ, băm và lưu mật khẩu mới. |
| F06 | Xem trang chủ | Tổng hợp phim, suất chiếu và khuyến mãi để hiển thị. |
| F07 | Danh sách và tìm kiếm phim | Truy vấn phim có tìm kiếm và phân trang. |
| F08 | Chi tiết phim và trailer | Cung cấp dữ liệu chi tiết cùng lịch chiếu/trailer hiện có. |
| F09 | Tra cứu hệ thống rạp | Truy vấn chi nhánh và nhóm theo tỉnh/thành. |
| F10 | Lịch chiếu tại rạp | Truy vấn suất chiếu theo chi nhánh và thời gian. |
| F11 | Lịch chiếu theo ngày | Xây truy vấn lịch theo ngày và rạp. |
| F12 | Chọn suất chiếu | Kiểm tra suất hợp lệ và chuyển tiếp sang đặt ghế. |
| F13 | Xem sơ đồ ghế | Cấp dữ liệu sơ đồ và trạng thái ghế của suất chiếu. |
| F14 | Chọn và bỏ chọn ghế | Xác thực ghế và tính giá chính thức ở server. |
| F15 | Giữ ghế và tạo đơn | Tạo booking trong giao dịch, khóa/chống đặt trùng và đặt thời hạn giữ. |
| F16 | Giải phóng ghế hết hạn | Chạy lịch giải phóng ghế/đơn quá hạn theo cấu hình. |
| F17 | Xem và áp dụng khuyến mãi | Kiểm tra mã, hạn, điều kiện tối thiểu, mức giảm và giới hạn sử dụng. |
| F18 | Thanh toán đơn | Tính lại tổng tiền, ghi nhận phương thức và đổi trạng thái đơn. |
| F19 | Thanh toán PayOS | Tạo checkout link, webhook và xác minh chữ ký giao dịch. |
| F20 | Phát hành vé điện tử | Sinh vé/mã QR sau thanh toán thành công. |
| F21 | Phát hành hóa đơn | Tạo hóa đơn gắn đơn và lưu các khoản tiền. |
| F22 | Lịch sử đặt và vé của tôi | Cấp truy vấn đơn/vé theo người dùng và kiểm tra quyền sở hữu. |
| F23 | Hủy đơn đặt vé | Kiểm tra điều kiện hủy và cập nhật trạng thái/ghế. |
| F24 | Bảng điều khiển quản trị | Cung cấp số liệu tổng hợp và danh sách đơn mới. |
| F25 | Quản lý phim | Cung cấp nghiệp vụ danh sách và các thao tác quản lý phim theo phạm vi. |
| F26 | Quản lý rạp, phòng, ghế | Cung cấp nghiệp vụ quản lý rạp/phòng/bố cục ghế theo phạm vi. |
| F27 | Quản lý suất chiếu | Tạo/sửa/hủy suất, kiểm tra thời gian và xung đột phòng. |
| F28 | Quản lý đơn đặt vé | Cung cấp danh sách có phân trang và chi tiết đơn cho admin. |
| F29 | Quản lý khuyến mãi | Cung cấp tạo/sửa/bật tắt/xóa theo điều kiện nghiệp vụ. |
| F30 | Quản lý người dùng | Cung cấp tra cứu và cập nhật trạng thái/quyền theo phạm vi. |
| F31 | Báo cáo doanh thu | Truy vấn tổng hợp theo thời gian/phim/rạp theo phạm vi thống nhất. |
| F32 | Đồng bộ TMDB/trailer | Kết nối API, ánh xạ dữ liệu và xử lý lỗi/giới hạn gọi. |
| F33 | Trợ lý AI tư vấn | Thiết kế service hội thoại và giới hạn dữ liệu gửi ra ngoài khi triển khai. |
| F34 | JWT, OTP hoặc OAuth | Thiết kế luồng xác thực/token theo phạm vi được phê duyệt. |
| F35 | Bán combo bắp nước | Xây nghiệp vụ danh mục/giá/tồn kho và gắn combo vào đơn. |
| F36 | Check-in vé QR | Xây endpoint xác thực vé và ghi nhận đã sử dụng. |

## Đầu ra phụ trách

- Nghiệp vụ/backend theo mã chức năng; xác thực, tích hợp, triển khai và phối hợp review.

