package org.springboot.smartdining.service;

import org.springboot.smartdining.entity.DiningTable;

import java.util.List;

public interface DiningTableService {

    DiningTable create(DiningTable input);

    DiningTable getById(Long id);

    List<DiningTable> getAll();

    DiningTable update(Long id, DiningTable input);

    /** Không xóa bản ghi, chỉ chuyển bàn sang INACTIVE. */
    void delete(Long id);
}
