package org.springboot.smartdining.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springboot.smartdining.dto.KitchenTaskResponse;

import java.util.List;

@Mapper
public interface KitchenMapper {

    /**
     * Món chờ làm và đang làm (KIT-1): của đơn tại bàn chưa trả và đơn mang về chưa hủy,
     * món cũ nhất trước.
     */
    List<KitchenTaskResponse> findTasks();
}
