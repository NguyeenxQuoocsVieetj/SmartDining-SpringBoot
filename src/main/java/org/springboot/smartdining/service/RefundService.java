package org.springboot.smartdining.service;

import org.springboot.smartdining.entity.Refund;

import java.util.List;

public interface RefundService {

    Refund create(Refund input);

    Refund getById(Long id);

    List<Refund> getAll();

    Refund update(Long id, Refund input);

    void delete(Long id);
}
