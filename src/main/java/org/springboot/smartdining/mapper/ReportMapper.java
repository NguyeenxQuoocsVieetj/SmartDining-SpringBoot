package org.springboot.smartdining.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springboot.smartdining.dto.OpenOrderDetailResponse;
import org.springboot.smartdining.dto.PaidDetailResponse;
import org.springboot.smartdining.dto.RevenueReportResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Truy vấn tổng hợp cho quản trị. Khoảng thời gian là [from, until); null là không giới hạn phía đó.
 */
@Mapper
public interface ReportMapper {

    /** Thu và hoàn theo từng ngày, chỉ ngày có phát sinh, tăng dần. */
    List<RevenueReportResponse.Daily> findDailyMoney(@Param("from") LocalDateTime from,
                                                     @Param("until") LocalDateTime until);

    ReportRows.OverviewCounts countOverview();

    /** Khoản đã thu trong khoảng, mới nhất trước. */
    List<PaidDetailResponse> findPayments(@Param("from") LocalDateTime from, @Param("until") LocalDateTime until);

    /** Đơn tại bàn chưa trả, cũ nhất trước; waitingOnly chỉ lấy bàn đang chờ thanh toán. */
    List<OpenOrderDetailResponse> findOpenOrders(@Param("waitingOnly") boolean waitingOnly);

    /** Mọi nhân viên, xếp theo vai trò, họ tên, id. */
    List<ReportRows.Staff> findStaff();
}
