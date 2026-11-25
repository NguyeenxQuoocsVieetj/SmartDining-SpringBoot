package org.springboot.smartdining.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springboot.smartdining.entity.TableRequest;

import java.util.List;

@Mapper
public interface TableRequestMapper {

    int insert(TableRequest tableRequest);

    TableRequest findById(Long id);

    List<TableRequest> findAll();

    int update(TableRequest tableRequest);

    int deleteById(Long id);
}
