package com.cinex.init;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Danh sách phim mẫu để dựng dữ liệu trang chủ và lịch chiếu.
 */
public final class MovieSeed {

    private MovieSeed() {
    }

    public record Entry(
            String title,
            String genre,
            Integer duration,
            String rating,
            String poster,
            String backdrop,
            String director,
            String description,
            LocalDate releaseDate,
            Integer ticketPrice,
            Integer vipPrice,
            String trailerUrl,
            BigDecimal popularity,
            String language,
            Integer tmdbId) {
    }

    public static List<Entry> entries() {
        return List.of(
                new Entry(
                        "Người Nhện: Khởi Đầu Mới", "Khoa học viễn tưởng, Hành động, Phiêu lưu", 150, "T13",
                        "https://image.tmdb.org/t/p/w500/wqGZVSCUSXE92WH2zyol2REaqT4.jpg", "https://image.tmdb.org/t/p/original/r08LxMJCbf1SiJIWeyH9aJmdqwC.jpg", "Destin Daniel Cretton",
                        "Không còn Tony Stark, MJ hay Ned kề cận, Peter buộc phải đơn thân độc mã đối diện với phe đối đầu bí ẩn. Tuy nhiên, khi áp lực ngày càng gia tăng, nó kích hoạt một sự biến đổi thể chất bất ngờ, đe dọa chính sự tồn tại của anh. Đồng thời, một chuỗi tội phạm bí ẩn mới xuất hiện, kéo theo một trong những mối đe dọa mạnh mẽ nhất mà Spider-Man từng đối mặt.",
                        LocalDate.of(2026, 7, 29), 75000, 120000,
                        "https://youtube.com/watch?v=RKev8MVJViU", new BigDecimal("655.44"), "en", 969681),
                new Entry(
                        "Thời Khắc Công Bố", "Khoa học viễn tưởng, Giật gân", 146, "P",
                        "https://image.tmdb.org/t/p/w500/nI4uPl4E3No8674XnyY8zJpceBS.jpg", "https://image.tmdb.org/t/p/original/gVczdWWAkBCuwEV1v9cg7ELfdhT.jpg", "Steven Spielberg",
                        "Nếu một ngày bạn nhận ra rằng chúng ta chưa bao giờ đơn độc, nếu có ai đó đứng trước mặt bạn và chứng minh điều đó là sự thật, liệu việc đó khiến bạn sợ hãi? Mùa Hè 2026, sự thật nằm trong tay của bảy tỷ con người. Chúng ta đang tiến rất gần đến… Thời Khắc Công Bố.",
                        LocalDate.of(2026, 6, 10), 75000, 120000,
                        "https://youtube.com/watch?v=QRGsUgeLJVE", new BigDecimal("477.84"), "en", 1275779),
                new Entry(
                        "Hope: Vùng Tử Địa", "Khoa học viễn tưởng, Bí ẩn, Hành động", 157, "P",
                        "https://image.tmdb.org/t/p/w500/yY5GFSFM4eQknenk2oCoC19EeF5.jpg", "https://image.tmdb.org/t/p/original/qqHFLg90IUSoTmSMpKOOW5RyqN3.jpg", "나홍진",
                        "Tại Hope Harbor, một ngôi làng hẻo lánh nằm gần khu vực phi quân sự, cảnh sát trưởng Bum-seok đã nhận được tin báo khẩn cấp từ người dân địa phương về sự xuất hiện của một con hổ. Khi cả ngôi làng chìm trong hoảng loạn, Bum-seok ngỡ ngàng khi phát hiện một sự thật nằm ngoài sức tưởng tượng của con người.",
                        LocalDate.of(2026, 7, 15), 75000, 120000,
                        null, new BigDecimal("368.50"), "ko", 1058424),
                new Entry(
                        "Matchbox Phim Điện Ảnh - Matchbox the Movie", "Hành động, Hài hước, Phiêu lưu", 127, "T13",
                        "https://image.tmdb.org/t/p/w500/l6Pp3m0L3kOaSyjkjUgl93C0IJg.jpg", "https://image.tmdb.org/t/p/original/jdJsnhHfkIi3J7jwfiCFT4gXNx5.jpg", "Sam Hargrave",
                        "Cuộc hội ngộ của một nhóm bạn thuở nhỏ bỗng rẽ vào một bước ngoặt bất ngờ khi cả bọn rơi vào một âm mưu quốc tế nguy hiểm và phải đứng lên cứu thế giới.",
                        LocalDate.of(2026, 10, 5), 75000, 120000,
                        null, new BigDecimal("363.66"), "en", 1148365),
                new Entry(
                        "Quỷ Quyệt: Ranh Giới Vô Định", "Kinh dị, Giật gân", 106, "T13",
                        "https://image.tmdb.org/t/p/w500/u1ZyMm7HcYO1CJwtk28dStDH9Bp.jpg", "https://image.tmdb.org/t/p/original/p40lmrdI4blcAPZQMTPaiy5v5Ym.jpg", "Jacob Chase",
                        "Ba kẻ rình rập xâm nhập một khu dân cư yên tĩnh và kéo một gia đình mới chuyển đến sang cõi linh hồn. Tại đó, họ phát hiện ra một sự thật kinh hoàng: cõi âm đang dần xâm lấn thế giới thực.",
                        LocalDate.of(2026, 8, 19), 75000, 120000,
                        null, new BigDecimal("358.60"), "en", 1291595),
                new Entry(
                        "Liên Khúc Phim Kinh Dị 6", "Hài hước, Kinh dị", 96, "P",
                        "https://image.tmdb.org/t/p/w500/4i90SNCZb3CFXVs5xlEjm5RuonP.jpg", "https://image.tmdb.org/t/p/original/xWBiXclrRmTggQHMRsIn84YHavs.jpg", "Michael Tiddes",
                        "Đang cập nhật",
                        LocalDate.of(2026, 6, 3), 75000, 120000,
                        "https://youtube.com/watch?v=d2cRh-PtPGQ", new BigDecimal("346.63"), "en", 1273221),
                new Entry(
                        "The Odyssey", "Phiêu lưu, Hành động, Kỳ ảo", 173, "T13",
                        "https://image.tmdb.org/t/p/w500/7876PK6gosE13Uq1B6nRxf7AJLT.jpg", "https://image.tmdb.org/t/p/original/nVBzWBqHTB3XD0iqWiJLfKLo5fq.jpg", "Christopher Nolan",
                        "Câu chuyện theo chân Odysseus trong hành trình kéo dài 10 năm trở về nhà sau cuộc chiến thành Troy, nơi ông phải đối mặt với các vị thần, quái vật và vô vàn thử thách, đồng thời nỗ lực đoàn tụ với vợ và giành lại vương quốc của mình.",
                        LocalDate.of(2026, 7, 15), 75000, 120000,
                        "https://youtube.com/watch?v=o4s236zGkMM", new BigDecimal("342.35"), "en", 1368337),
                new Entry(
                        "Hành Trình Của Moana", "Gia đình, Kỳ ảo, Hài hước, Phiêu lưu", 115, "P",
                        "https://image.tmdb.org/t/p/w500/lwqKqiBsGVBtQBZF9ZTunthBCyI.jpg", "https://image.tmdb.org/t/p/original/mMkJq4dkQwfDieB9wRC9yPxDWv9.jpg", "Thomas Kail",
                        "Nghe theo tiếng gọi của Đại Dương, Moana dũng cảm rời đảo Motunui, lần đầu vượt qua rạn san hô để khám phá thế giới rộng lớn. Cùng á thần Maui, cô bắt đầu hành trình phiêu lưu đầy kỳ diệu với sứ mệnh mang lại sự thịnh vượng cho quê hương.",
                        LocalDate.of(2026, 7, 8), 75000, 120000,
                        "https://youtube.com/watch?v=0169_rL0EPo", new BigDecimal("284.10"), "en", 1108427),
                new Entry(
                        "Cú Rơi Tử Thần 2: Điểm Chết", "Giật gân", 98, "T13",
                        "https://image.tmdb.org/t/p/w500/fgSm5ylwiXbIHn8UbUXDjk9RRu4.jpg", "https://image.tmdb.org/t/p/original/nGau0KBOaRILSK5MqFcuTfv7GMl.jpg", "Michael Spierig",
                        "Sau cái chết của chị gái Hunter, Jax tìm thấy chút an ủi nơi Luce, người bạn gan dạ từng thân thiết với Hunter. Để đối diện nỗi mất mát, họ cùng thử thách cung đường ván treo khét tiếng trên núi Kwan ở Thái Lan, nhưng một vụ sạt lở bất ngờ khiến Jax mắc kẹt ở độ cao 3,000 feet, nơi chỉ một bước sai cũng có thể là dấu chấm hết.",
                        LocalDate.of(2026, 9, 1), 75000, 120000,
                        null, new BigDecimal("281.62"), "en", 1101412),
                new Entry(
                        "Backrooms: Thực Thể Quỷ Quyệt", "Kinh dị, Bí ẩn, Khoa học viễn tưởng", 111, "T13",
                        "https://image.tmdb.org/t/p/w500/7uabN7xF4209cDUdBuMvkVcpkN2.jpg", "https://image.tmdb.org/t/p/original/dqmMWNWfLnExDRpMtIMqI97GQFR.jpg", "Kane Parsons",
                        "Backrooms theo chân Clark, một chủ cửa hàng nội thất, vô tình phát hiện cánh cửa bí ẩn dưới tầng hầm. Bước qua đó, anh bị cuốn vào một chiều không gian vô tận với những căn phòng màu vàng méo mó, liên tục lặp lại. Khi Clark ngày càng lún sâu và ám ảnh, nhà trị liệu tâm lý của anh là Mary quyết định bước vào không gian đó để tìm và giải cứu anh.",
                        LocalDate.of(2026, 5, 27), 75000, 120000,
                        "https://youtube.com/watch?v=s5vWwGFiS8M", new BigDecimal("280.32"), "en", 1083381),
                new Entry(
                        "Cơn Thịnh Nộ", "Hành động, Hình sự, Giật gân", 113, "T13",
                        "https://image.tmdb.org/t/p/w500/pomewNtFVHcox6UIOaNLQLHTaae.jpg", "https://image.tmdb.org/t/p/original/1AVF2fAevpfi2HP6AEpptG1kg8R.jpg", "谷垣健治",
                        "Cơn Thịnh Nộ",
                        LocalDate.of(2026, 6, 10), 75000, 120000,
                        "https://youtube.com/watch?v=IkNg-QmSyqE", new BigDecimal("260.73"), "zh", 1280738),
                new Entry(
                        "Khách", "Kinh dị, Giật gân", 94, "T13",
                        "https://image.tmdb.org/t/p/w500/39z7di5bi8tW4696SqhN96jxK58.jpg", "https://image.tmdb.org/t/p/original/3pDfu71mrzSuFYyIfnKEjt8z4BC.jpg", "André Øvredal",
                        "Vài tuần sau khi bắt đầu hành trình sống trên xe van, một cặp đôi trẻ chứng kiến một vụ tai nạn kinh hoàng khiến người tài xế thiệt mạng. Không lâu sau, họ bị một thực thể quỷ dị bám theo ráo riết — thứ không thể bị cắt đuôi và luôn lần ra họ dù họ chạy đến bất cứ đâu.",
                        LocalDate.of(2026, 5, 20), 75000, 120000,
                        "https://youtube.com/watch?v=m2VIZbPhfk8", new BigDecimal("242.91"), "en", 1368314),
                new Entry(
                        "Phi Vụ Sửa Sai", "Hành động, Giật gân", 142, "P",
                        "https://image.tmdb.org/t/p/w500/jlP8XDjZN89GrpM3je5Uf8Grqx8.jpg", "https://image.tmdb.org/t/p/original/dJTWIecL2vxsCRl5G0lRhPsfrhc.jpg", "Guy Moshe",
                        "Sau một nhiệm vụ giải cứu thất bại ở Afghanistan, một nhóm cựu đặc vụ CIA mang trong mình cảm giác tội lỗi và thất bại. Khi người chỉ huy Larry tiết lộ bí mật về 25 triệu USD được cất giấu tại Tehran, Tucker tập hợp cả nhóm trở lại Iran, vừa nhằm kiếm một khoản tiền khổng lồ, vừa tìm cách chuộc lại những sai lầm trong quá khứ. Nhưng nhiệm vụ nhanh chóng biến thành một cuộc giải cứu nguy hiểm với những âm mưu, phản bội và bí mật tình báo bị che giấu suốt nhiều năm.",
                        LocalDate.of(2026, 9, 10), 75000, 120000,
                        null, new BigDecimal("237.13"), "en", 1228834),
                new Entry(
                        "Bản năng thú", "Giật gân, Chính kịch", 109, "P",
                        "https://image.tmdb.org/t/p/w500/iISM36voS1E8gX9BM0DVK0kgi3E.jpg", "https://image.tmdb.org/t/p/original/tzoarEu7TsG959upiELyYPm5kO9.jpg", "Ben Affleck",
                        "Khi con trai họ bị bắt cóc, một ứng viên thị trưởng Los Angeles và vợ cuống cuồng xoay xở tiền chuộc, đồng thời phải đưa ra những lựa chọn có thể phá tan thế giới của họ.",
                        LocalDate.of(2026, 10, 9), 75000, 120000,
                        null, new BigDecimal("236.84"), "en", 1236045)
        );
    }
}
