package org.springboot.smartdining.service;

import org.springboot.smartdining.dto.TakeawayResponse;

import java.util.List;

/** Đơn mang về ở quầy. */
public interface TakeawayService {

    /** Đơn lấy trong ngày hôm nay, mới nhất trước. */
    List<TakeawayResponse> getToday();
}
