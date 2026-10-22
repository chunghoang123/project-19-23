package com.cinex.init;

import java.util.List;

/**
 * Dữ liệu các chi nhánh rạp CineX.
 *
 * <p>Địa chỉ thực tế được tái sử dụng từ mạng lưới rạp cùng hệ thống trước đây,
 * đổi thương hiệu về CineX. Mỗi chi nhánh thuộc một tỉnh/thành phố.</p>
 */
public final class CinemaSeed {

    private CinemaSeed() {
    }

    /** Tên chi nhánh, địa chỉ và tỉnh/thành. */
    public record Entry(String name, String address, String provinceName) {
    }

    public static List<Entry> entries() {
        return List.of(
                new Entry("CineX Bắc Giang", "Tầng trệt, TTTM Go! Bắc Giang, X. Tân Tiến, Tp. Bắc Giang, Bắc Giang", "Bắc Giang"),
                new Entry("CineX Bắc Ninh", "Tầng 3, TTTM Vincom Bắc Ninh, Ngã 6, P. Suối Hoa, Tp. Bắc Ninh, Bắc Ninh", "Bắc Ninh"),
                new Entry("CineX Bảo Lộc", "Tầng 3, TTTM Vincom Plaza Bảo Lộc, 83 Lê Hồng Phong, P.1, Tp. Bảo Lộc, Lâm Đồng", "Lâm Đồng"),
                new Entry("CineX Biên Hòa", "Tầng 5, TTTM Vincom Biên Hòa, 1096 Phạm Văn Thuận, KP2, P. Tân Mai, Tp. Biên Hòa, Đồng Nai", "Đồng Nai"),
                new Entry("CineX Bình Dương", "Tầng 2, Lotte Mart Bình Dương, P. Lái Thiêu, Tx. Thuận An, Bình Dương", "Bình Dương"),
                new Entry("CineX Cà Mau", "Tầng 4, TTTM Vincom Cà Mau, Đ. Lê Duẩn, P.1, Tp. Cà Mau, Cà Mau", "Cà Mau"),
                new Entry("CineX Cần Thơ Cái Răng", "Tầng 1, TTTM Go! Cần Thơ, P. Hưng Phú, Q. Cái Răng, Tp. Cần Thơ", "Cần Thơ"),
                new Entry("CineX Cần Thơ Ninh Kiều", "Lầu 3, TTTM Lotte Mart, 84 Mậu Thân, P. An Hòa, Q. Ninh Kiều, Tp. Cần Thơ", "Cần Thơ"),
                new Entry("CineX Cantavil", "Tầng 7, Cantavil Premier, Số 1 Song Hành, Xa lộ Hà Nội, P. An Phú, Q.2, TP. Hồ Chí Minh", "TP. Hồ Chí Minh"),
                new Entry("CineX Cộng Hòa", "Tầng 4, Pico Plaza, 20 Cộng Hòa, P.12, Q. Tân Bình, TP. Hồ Chí Minh", "TP. Hồ Chí Minh"),
                new Entry("CineX Dĩ An", "Tầng 5, TTTM Vincom Plaza Dĩ An, Số 3 DT743, KP. Thống Nhất, Tx. Dĩ An, Bình Dương", "Bình Dương"),
                new Entry("CineX Đà Nẵng", "Tầng 5&6, Lotte Mart Đà Nẵng, 6 Nại Nam, P. Hoà Cường Bắc, Q. Hải Châu, Tp. Đà Nẵng", "Đà Nẵng"),
                new Entry("CineX Đồng Hới", "Tầng 3, TTTM Vincom Plaza Đồng Hới, P. Hải Đình, Tp. Đồng Hới, Quảng Bình", "Quảng Bình"),
                new Entry("CineX Đồng Nai", "Lotte Mart Biên Hòa, Quốc lộ 1A, P. Long Bình, Tp. Biên Hoà, Đồng Nai", "Đồng Nai"),
                new Entry("CineX Gò Vấp", "Tầng 3, Lotte Mart Gò Vấp, 242 Nguyễn Văn Lượng, Q. Gò Vấp, TP. Hồ Chí Minh", "TP. Hồ Chí Minh"),
                new Entry("CineX Gold View", "Tầng 3, TTTM TNL Plaza, 346 Bến Vân Đồn, P.1, Q.4, TP. Hồ Chí Minh", "TP. Hồ Chí Minh"),
                new Entry("CineX Hà Đông", "Tầng 4, Mê Linh Plaza Hà Đông, Đ. Tô Hiệu, P. Hà Cầu, Q. Hà Đông, Hà Nội", "Hà Nội"),
                new Entry("CineX Hạ Long", "Tầng 1, TTTM Go! Hạ Long, P. Hồng Hải, Tp. Hạ Long, Quảng Ninh", "Quảng Ninh"),
                new Entry("CineX Hải Dương", "Tầng 4, TTTM Đỗ Gia Palace, 158 Ngô Quyền, P. Thanh Bình, Tp. Hải Dương, Hải Dương", "Hải Dương"),
                new Entry("CineX Hải Phòng", "Tầng 5, TTTM Vincom Plaza, 1 Lê Thánh Tông, P. Máy Tơ, Q. Ngô Quyền, Tp. Hải Phòng", "Hải Phòng"),
                new Entry("CineX Hội An", "The Pearl Hội An, An Bàng, Cẩm An, Tp. Hội An, Quảng Nam", "Quảng Nam"),
                new Entry("CineX Huế", "Tầng 3, TTTM Go! Huế, P. Phú Hội, Tp. Huế, Thừa Thiên Huế", "Thừa Thiên Huế"),
                new Entry("CineX Kosmo", "Tầng 2, Kosmo Tây Hồ, 161 Xuân La, P. Xuân Tảo, Q. Bắc Từ Liêm, Hà Nội", "Hà Nội"),
                new Entry("CineX Long Xuyên", "Tầng 5, TTTM Vincom Long Xuyên, P. Mỹ Bình, Tp. Long Xuyên, An Giang", "An Giang"),
                new Entry("CineX Moonlight", "Tầng 2, Moonlight Residences, 102 Đặng Văn Bi, P. Bình Thọ, Q. Thủ Đức, TP. Hồ Chí Minh", "TP. Hồ Chí Minh"),
                new Entry("CineX Nam Định", "Tầng 5, TTTM Nam Định Tower, 91 Điện Biên, P. Cửa Bắc, Tp. Nam Định, Nam Định", "Nam Định"),
                new Entry("CineX Nam Sài Gòn", "Tầng 3, TTTM Lotte, 469 Nguyễn Hữu Thọ, P. Tân Hưng, Q.7, TP. Hồ Chí Minh", "TP. Hồ Chí Minh"),
                new Entry("CineX Nha Trang Thái Nguyên", "Tầng 5, 60 Thái Nguyên, P. Phương Sài, Tp. Nha Trang, Khánh Hòa", "Khánh Hòa"),
                new Entry("CineX Nha Trang Trần Phú", "Tầng 4, Vinpearl Beachfront Condotel, 78-80 Trần Phú, P. Lộc Thọ, Tp. Nha Trang, Khánh Hòa", "Khánh Hòa"),
                new Entry("CineX Ninh Bình", "Tầng 1, TTTM Go! Ninh Bình, X. Ninh Phúc, Tp. Ninh Bình, Ninh Bình", "Ninh Bình"),
                new Entry("CineX Nowzone", "Tầng 5, TTTM Nowzone, 235 Nguyễn Văn Cừ, P. Nguyễn Cư Trinh, Q.1, TP. Hồ Chí Minh", "TP. Hồ Chí Minh"),
                new Entry("CineX Phan Rang", "Tầng 3, TTTM Vincom Phan Rang, 122 Mười Sáu Tháng Tư, Mỹ Hải, Phan Rang - Tháp Chàm, Ninh Thuận", "Ninh Thuận"),
                new Entry("CineX Phan Thiết", "Tầng 5, TTTM Lotte Mart, P. Phú Thuỷ, Tp. Phan Thiết, Bình Thuận", "Bình Thuận"),
                new Entry("CineX Phủ Lý", "Tầng 4, TTTM Vincom Plaza Phủ Lý, 60 Biên Hòa, P. Minh Khai, Tp. Phủ Lý, Hà Nam", "Hà Nam"),
                new Entry("CineX Phú Thọ", "Tầng 4, Lotte Mart Phú Thọ, 968 Ba Tháng Hai, P.15, Q.11, TP. Hồ Chí Minh", "TP. Hồ Chí Minh"),
                new Entry("CineX Tây Ninh", "Tầng 4, TTTM TTC Plaza Tây Ninh, 217-219 Đường 30/4, P.2, Tp. Tây Ninh, Tây Ninh", "Tây Ninh"),
                new Entry("CineX Thái Bình", "Tầng 5, TTTM Vincom, 460 Lý Bôn, P. Đề Thám, Tp. Thái Bình, Thái Bình", "Thái Bình"),
                new Entry("CineX Thăng Long", "Tầng 3, TTTM Big C Thăng Long, 222 Trần Duy Hưng, P. Trung Hoà, Q. Cầu Giấy, Hà Nội", "Hà Nội"),
                new Entry("CineX Thanh Hóa", "Tầng 4, TTTM Vincom Plaza Thanh Hoá, 27 Trần Phú, P. Điện Biên, Tp. Thanh Hoá, Thanh Hóa", "Thanh Hóa"),
                new Entry("CineX Thủ Đức", "Tầng 2, Joy Citipoint, KCX Linh Trung, KP4, P. Linh Trung, Q. Thủ Đức, TP. Hồ Chí Minh", "TP. Hồ Chí Minh"),
                new Entry("CineX Tuyên Quang", "Tầng 3, TTTM Vincom Plaza Tuyên Quang, 260 Quang Trung, Tổ 35, P. Phan Thiết, Tp. Tuyên Quang, Tuyên Quang", "Tuyên Quang"),
                new Entry("CineX Việt Trì", "Tầng 5, TTTM Vincom Việt Trì, P. Tiên Cát, Tp. Việt Trì, Phú Thọ", "Phú Thọ"),
                new Entry("CineX Vinh", "Tầng 5, 1 Phan Bội Châu, P. Quán Bàu, Tp. Vinh, Nghệ An", "Nghệ An"),
                new Entry("CineX Vũng Tàu", "Tầng 3, TTTM Lotte Vũng Tàu, P.8, Tp. Vũng Tàu, Bà Rịa - Vũng Tàu", "Bà Rịa - Vũng Tàu"),
                new Entry("CineX West Lake", "Tầng 4, Lotte Mall West Lake Hanoi, 683 Lạc Long Quân, Tây Hồ, Hà Nội", "Hà Nội")
        );
    }
}
