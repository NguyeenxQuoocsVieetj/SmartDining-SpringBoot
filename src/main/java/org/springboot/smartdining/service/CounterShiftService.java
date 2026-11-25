package org.springboot.smartdining.service;

import org.springboot.smartdining.entity.CounterShift;

import java.util.List;

public interface CounterShiftService {

    CounterShift create(CounterShift input);

    CounterShift getById(Long id);

    List<CounterShift> getAll();

    CounterShift update(Long id, CounterShift input);

    void delete(Long id);
}
