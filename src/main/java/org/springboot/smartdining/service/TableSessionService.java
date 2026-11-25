package org.springboot.smartdining.service;

import org.springboot.smartdining.entity.TableSession;

import java.util.List;

public interface TableSessionService {

    /** Phiên tạo từ quản trị không trả token gốc cho ai, khách lấy phiên qua mã QR. */
    TableSession create(TableSession input);

    TableSession getById(Long id);

    List<TableSession> getAll();

    TableSession update(Long id, TableSession input);

    void delete(Long id);
}
