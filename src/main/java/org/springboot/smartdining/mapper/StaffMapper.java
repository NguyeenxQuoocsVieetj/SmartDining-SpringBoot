package org.springboot.smartdining.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springboot.smartdining.dto.StaffItemResponse;
import org.springboot.smartdining.dto.StaffRequestResponse;

import java.util.List;

@Mapper
public interface StaffMapper {

    /** Món READY của các đơn tại bàn chưa trả, món cũ nhất trước. */
    List<StaffItemResponse> findReadyItems();

    /** Dòng món của một món tại bàn, bất kể trạng thái. */
    StaffItemResponse findItemById(Long itemId);

    /** Yêu cầu đang chờ, cũ nhất trước; amountDue để trống cho service điền. */
    List<StaffRequestResponse> findPendingRequests();
}
