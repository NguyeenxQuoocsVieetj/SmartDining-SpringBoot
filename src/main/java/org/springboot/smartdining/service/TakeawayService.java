package org.springboot.smartdining.service;

import org.springboot.smartdining.dto.TakeawayOrderRequest;
import org.springboot.smartdining.dto.TakeawayResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Đơn mang về ở quầy. */
public interface TakeawayService {

    /** Đơn lấy trong ngày hôm nay, mới nhất trước. */
    List<TakeawayResponse> getToday();

    /**
     * Giao đơn cho khách: mọi món READY thành SERVED, ghi nhân viên đang trực quầy (CNT-5).
     * Chỉ giao được khi không còn món chờ làm hoặc đang làm; không phụ thuộc ngày lấy.
     */
    TakeawayResponse deliver(Long orderId);

    /**
     * Lên đơn mang về đã trả tiền (TKW-1 đến TKW-3, TKW-6): món vào thẳng bếp, cấp mã lấy đồ,
     * ghi khoản thu vào ca đang mở. Dữ liệu phải qua validation của {@link TakeawayOrderRequest}.
     *
     * @param proof ảnh bill, bắt buộc khi chuyển khoản, bỏ qua khi tiền mặt
     */
    TakeawayResponse create(TakeawayOrderRequest request, MultipartFile proof);
}
