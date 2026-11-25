package org.springboot.smartdining.service;

import org.springboot.smartdining.entity.DailyPickupCounter;

import java.time.LocalDate;
import java.util.List;

public interface DailyPickupCounterService {

    DailyPickupCounter create(DailyPickupCounter input);

    DailyPickupCounter getById(LocalDate businessDate);

    List<DailyPickupCounter> getAll();

    DailyPickupCounter update(LocalDate businessDate, DailyPickupCounter input);

    void delete(LocalDate businessDate);

    /**
     * Cấp mã lấy đồ tiếp theo của ngày, an toàn khi nhiều người tạo cùng lúc.
     * Hai câu lệnh phải chạy trên cùng kết nối, nên bắt buộc nằm trong một transaction.
     */
    int nextCode(LocalDate businessDate);
}
