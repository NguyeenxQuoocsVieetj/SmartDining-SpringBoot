package org.springboot.smartdining.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springboot.smartdining.enums.PaymentMethod;

import java.util.List;

/**
 * Đơn mang về khách trả tiền trước (TKW-1, TKW-2, TKW-7). Chuỗi rỗng coi như không gửi.
 * Không có giá: server lấy giá hiện tại của món.
 *
 * @param pickupTime giờ lấy dạng HH:mm; đã qua quá 5 phút thì hiểu là ngày mai
 */
public record TakeawayOrderRequest(
        @Size(max = 100) String customerName,
        @Pattern(regexp = "[0-9+ .-]{8,15}", message = "dài 8-15 ký tự, chỉ gồm số, +, khoảng trắng, . -")
        String customerPhone,
        @Pattern(regexp = "([01][0-9]|2[0-3]):[0-5][0-9]", message = "phải có dạng HH:mm") String pickupTime,
        @NotNull PaymentMethod method,
        @NotNull @Size(min = 1, max = 50) List<@NotNull @Valid Item> items) {

    public record Item(
            @NotNull Long productId,
            @NotNull @Min(1) @Max(100) Integer quantity,
            @Size(max = 255) String note) {

        public Item {
            note = clean(note);
        }
    }

    public TakeawayOrderRequest {
        customerName = clean(customerName);
        customerPhone = clean(customerPhone);
        pickupTime = clean(pickupTime);
    }

    /** Bỏ khoảng trắng hai đầu; rỗng thành null. */
    private static String clean(String text) {
        return text == null || text.isBlank() ? null : text.strip();
    }
}
