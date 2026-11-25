package org.springboot.smartdining.service;

import org.springboot.smartdining.entity.Payment;

import java.util.List;

public interface PaymentService {

    Payment create(Payment input);

    Payment getById(Long id);

    List<Payment> getAll();

    Payment update(Long id, Payment input);

    void delete(Long id);
}
