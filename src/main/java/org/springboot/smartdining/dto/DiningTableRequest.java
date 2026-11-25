package org.springboot.smartdining.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springboot.smartdining.entity.DiningTable;
import org.springboot.smartdining.enums.TableStatus;

/** version bắt buộc khi cập nhật, bỏ qua khi tạo mới. */
public record DiningTableRequest(
        @NotBlank @Size(max = 50) String name,
        TableStatus status,
        Integer version) {

    public DiningTable toEntity() {
        DiningTable table = new DiningTable();
        table.setName(name);
        table.setStatus(status);
        table.setVersion(version);
        return table;
    }
}
