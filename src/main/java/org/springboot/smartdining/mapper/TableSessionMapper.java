package org.springboot.smartdining.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springboot.smartdining.entity.TableSession;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TableSessionMapper {

    int insert(TableSession tableSession);

    TableSession findById(Long id);

    List<TableSession> findAll();

    /** tokenHash null thì giữ token đang có. */
    int update(TableSession tableSession);

    int deleteById(Long id);

    TableSession findByTokenHash(String tokenHash);

    /** Đóng mọi phiên còn mở của bàn, token của khách hết hiệu lực. */
    int closeOpenByTableId(@Param("tableId") Long tableId, @Param("closedAt") LocalDateTime closedAt);
}
