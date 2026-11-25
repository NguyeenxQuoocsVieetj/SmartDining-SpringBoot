package org.springboot.smartdining.service;

import org.springboot.smartdining.dto.StaffItemResponse;
import org.springboot.smartdining.dto.StaffOrderResponse;
import org.springboot.smartdining.dto.StaffRequestResponse;
import org.springboot.smartdining.dto.StaffTableResponse;
import org.springboot.smartdining.dto.TableRequestResponse;

import java.util.List;

/** Màn hình của nhân viên phục vụ. */
public interface StaffService {

    /** Sơ đồ bàn (STF-1), ẩn bàn INACTIVE. Đọc bàn, đơn, món mỗi thứ một lần. */
    List<StaffTableResponse> getTables();

    StaffOrderResponse getOrder(Long tableId);

    /** Món đã xong chờ mang ra tại bàn. Đơn mang về giao ở quầy nên không nằm đây. */
    List<StaffItemResponse> getReadyItems();

    /** Mang món ra bàn (STF-2): chỉ món READY. */
    StaffItemResponse serve(Long itemId);

    /** Yêu cầu đang chờ; yêu cầu thanh toán kèm số tiền lấy từ đơn đang mở của bàn (REQ-4). */
    List<StaffRequestResponse> getPendingRequests();

    /**
     * Đánh dấu đã xử lý một yêu cầu gọi phục vụ và ghi người, giờ xử lý (REQ-1, REQ-2).
     * Yêu cầu thanh toán chỉ đóng được khi thanh toán (STF-3).
     */
    TableRequestResponse completeRequest(Long requestId);
}
