package org.springboot.smartdining.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springboot.smartdining.entity.Payment;

import java.util.List;

@Mapper
public interface PaymentMapper {

    int insert(Payment payment);

    Payment findById(Long id);

    List<Payment> findAll();

    int update(Payment payment);

    int deleteById(Long id);

    /** Khoản thu đã hoàn tất của đơn; database bảo đảm tối đa một. */
    Payment findCompletedByOrderId(Long orderId);
}
