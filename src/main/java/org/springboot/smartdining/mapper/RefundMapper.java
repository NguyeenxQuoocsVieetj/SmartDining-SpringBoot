package org.springboot.smartdining.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springboot.smartdining.dto.PendingRefundResponse;
import org.springboot.smartdining.entity.Refund;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface RefundMapper {

    int insert(Refund refund);

    Refund findById(Long id);

    List<Refund> findAll();

    int update(Refund refund);

    int deleteById(Long id);

    int countByOrderItemId(Long orderItemId);

    /** Tổng đã hoàn của một khoản thu, 0 nếu chưa hoàn lần nào. */
    BigDecimal sumByPaymentId(Long paymentId);

    /** Món mang về đã hủy, đã thu tiền mà chưa hoàn; hủy sớm nhất trước. */
    List<PendingRefundResponse> findPendingTakeawayRefunds();
}
