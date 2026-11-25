package org.springboot.smartdining.service;

import org.springboot.smartdining.dto.TakeawayResponse;

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
}
