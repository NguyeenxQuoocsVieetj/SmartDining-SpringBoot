package org.springboot.smartdining.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springboot.smartdining.entity.DiningTable;

import java.util.List;

@Mapper
public interface DiningTableMapper {

    /** Chỉ dùng qua {@link #insert(DiningTable)}. */
    int insertRow(DiningTable diningTable);

    /** Chỉ dùng qua {@link #update(DiningTable)}. */
    int updateRow(DiningTable diningTable);

    default int insert(DiningTable diningTable) {
        int rows = insertRow(diningTable);
        if (rows == 1 && diningTable.getVersion() == null) {
            diningTable.setVersion(0);
        }
        return rows;
    }

    DiningTable findById(Long id);

    List<DiningTable> findAll();

    /** Trả 0 khi không có id hoặc version đã cũ. */
    default int update(DiningTable diningTable) {
        int rows = updateRow(diningTable);
        if (rows == 1) {
            diningTable.setVersion(diningTable.getVersion() + 1);
        }
        return rows;
    }

    int deleteById(Long id);
}
