package org.springboot.smartdining.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springboot.smartdining.entity.TableSession;

import java.util.List;

@Mapper
public interface TableSessionMapper {

    int insert(TableSession tableSession);

    TableSession findById(Long id);

    List<TableSession> findAll();

    /** tokenHash null thì giữ token đang có. */
    int update(TableSession tableSession);

    int deleteById(Long id);
}
