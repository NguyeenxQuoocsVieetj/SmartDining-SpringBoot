package org.springboot.smartdining.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springboot.smartdining.entity.TableRequest;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TableRequestMapper {

    int insert(TableRequest tableRequest);

    TableRequest findById(Long id);

    List<TableRequest> findAll();

    int update(TableRequest tableRequest);

    int deleteById(Long id);

    int deletePendingPaymentByTableId(Long tableId);

    /** Yêu cầu thanh toán đang chờ của bàn; database bảo đảm tối đa một. */
    TableRequest findPendingPaymentByTableId(Long tableId);

    /** Đóng mọi yêu cầu đang chờ của bàn. */
    int completePendingByTableId(@Param("tableId") Long tableId,
                                 @Param("completedBy") Long completedBy,
                                 @Param("completedAt") LocalDateTime completedAt);
}
