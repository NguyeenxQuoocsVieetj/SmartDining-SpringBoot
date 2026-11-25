package org.springboot.smartdining.service;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.KitchenTableResponse;
import org.springboot.smartdining.dto.KitchenTaskResponse;
import org.springboot.smartdining.enums.TableStatus;
import org.springboot.smartdining.mapper.DiningTableMapper;
import org.springboot.smartdining.mapper.KitchenMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Màn hình bếp. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KitchenService {

    private final KitchenMapper kitchenMapper;
    private final DiningTableMapper diningTableMapper;

    public List<KitchenTaskResponse> getTasks() {
        return kitchenMapper.findTasks();
    }

    /** Bàn đang sử dụng được, tức không INACTIVE. */
    public List<KitchenTableResponse> getTables() {
        return diningTableMapper.findAll().stream()
                .filter(table -> table.getStatus() != TableStatus.INACTIVE)
                .map(KitchenTableResponse::from)
                .toList();
    }
}
