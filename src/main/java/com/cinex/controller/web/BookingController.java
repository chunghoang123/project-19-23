package com.cinex.controller.web;

import com.cinex.domain.Booking;
import com.cinex.domain.User;
import com.cinex.dto.CheckoutForm;
import com.cinex.repository.BillRepository;
import com.cinex.repository.TicketRepository;
import com.cinex.security.CurrentUser;
import com.cinex.service.BookingService;
import com.cinex.service.PayOSService;
import com.cinex.service.PromotionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final PromotionService promotionService;
    private final PayOSService payOSService;
    private final TicketRepository ticketRepository;
    private final BillRepository billRepository;
    private final CurrentUser currentUser;

    @PostMapping("/checkout")
    public String checkout(@org.springframework.web.bind.annotation.ModelAttribute CheckoutForm form,
                           RedirectAttributes redirectAttributes) {
        User user = currentUser.require();
        Booking booking = bookingService.holdSeats(user, form);
        redirectAttributes.addFlashAttribute("success",
                "Đã giữ " + form.getSeatIds().size() + " ghế. Hoàn tất thanh toán trong 15 phút.");
        return "redirect:/booking/" + booking.getCode();
    }

    @GetMapping("/booking/{code}")
    public String detail(@PathVariable String code, Model model, RedirectAttributes redirectAttributes) {
        User user = currentUser.require();
        Booking booking = bookingService.getByCode(code);

        if (!booking.getUser().getId().equals(user.getId()) && !user.isAdmin()) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền xem đơn đặt vé này");
            return "redirect:/my-tickets";
        }

        model.addAttribute("booking", booking);
        model.addAttribute("tickets", ticketRepository.findByBookingIdOrderByIdAsc(booking.getId()));
        model.addAttribute("bill", billRepository.findByBookingId(booking.getId()).orElse(null));
        model.addAttribute("promotions", promotionService.findActive());
        model.addAttribute("payosEnabled", payOSService.isEnabled());
        return "booking/detail";
    }

    @PostMapping("/booking/{code}/confirm")
    public String confirm(@PathVariable String code,
                          @RequestParam(required = false) String promotionCode,
                          @RequestParam(defaultValue = "cash") String paymentMethod,
                          RedirectAttributes redirectAttributes) {
        User user = currentUser.require();
        Booking booking = bookingService.confirmBooking(user, code, promotionCode, paymentMethod);
        redirectAttributes.addFlashAttribute("success",
                "Thanh toán thành công. Mã đơn: " + booking.getCode());
        return "redirect:/booking/" + booking.getCode();
    }

    @PostMapping("/booking/{code}/cancel")
    public String cancel(@PathVariable String code, RedirectAttributes redirectAttributes) {
        User user = currentUser.require();
        bookingService.cancel(user, code);
        redirectAttributes.addFlashAttribute("success", "Đã hủy đơn đặt vé và trả lại ghế.");
        return "redirect:/my-tickets";
    }

    /**
     * Tạo link thanh toán online qua PayOS rồi chuyển người dùng sang trang thanh toán.
     */
    @PostMapping("/booking/{code}/payos")
    public String payWithPayOS(@PathVariable String code,
                               @RequestParam(required = false) String promotionCode,
                               RedirectAttributes redirectAttributes,
                               HttpServletRequest request) {
        User user = currentUser.require();
        Booking booking = bookingService.prepareOnlinePayment(user, code, promotionCode);
        PayOSService.PaymentLink link = payOSService.createPaymentLink(
                booking, user,
                absoluteUrl(request, "/booking/" + booking.getCode() + "/payos/return"),
                absoluteUrl(request, "/booking/" + booking.getCode()));
        return "redirect:" + link.checkoutUrl();
    }

    /** PayOS chuyển người dùng về sau khi thanh toán xong. */
    @GetMapping("/booking/{code}/payos/return")
    public String payOSReturn(@PathVariable String code, RedirectAttributes redirectAttributes) {
        Booking booking = bookingService.getByCode(code);
        if (Booking.STATUS_CONFIRMED.equals(booking.getStatus())) {
            redirectAttributes.addFlashAttribute("success",
                    "Thanh toán thành công. Mã đơn: " + booking.getCode());
        } else {
            redirectAttributes.addFlashAttribute("error",
                    "Chưa ghi nhận được thanh toán. Nếu bạn đã thanh toán, hãy tải lại trang sau ít phút.");
        }
        return "redirect:/booking/" + booking.getCode();
    }

    private String absoluteUrl(HttpServletRequest request, String path) {
        return request.getRequestURL().toString().replace(request.getRequestURI(), "") + path;
    }

    @GetMapping("/my-tickets")
    public String myTickets(Model model) {
        User user = currentUser.require();
        model.addAttribute("bookings", bookingService.findByUser(user.getId()));
        return "booking/my-tickets";
    }
}
