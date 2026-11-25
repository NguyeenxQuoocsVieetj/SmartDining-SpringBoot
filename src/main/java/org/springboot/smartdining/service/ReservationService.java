package org.springboot.smartdining.service;

import org.springboot.smartdining.entity.Reservation;

import java.util.List;

public interface ReservationService {

    Reservation create(Reservation input);

    Reservation getById(Long id);

    List<Reservation> getAll();

    Reservation update(Long id, Reservation input);

    void delete(Long id);
}
