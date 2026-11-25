package org.springboot.smartdining.service;

import org.springboot.smartdining.entity.CounterShift;

import java.util.List;

public interface CounterShiftService {

    CounterShift create(CounterShift input);

    CounterShift getById(Long id);

    List<CounterShift> getAll();

    CounterShift update(Long id, CounterShift input);

    void delete(Long id);

    /** Admin đóng ca hộ khi nhân viên quên đăng xuất (CNT-7); ca đã đóng thì 409. */
    CounterShift close(Long id);
}
