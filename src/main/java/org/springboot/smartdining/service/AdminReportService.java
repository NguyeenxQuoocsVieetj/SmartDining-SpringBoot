package org.springboot.smartdining.service;

import org.springboot.smartdining.dto.RevenueReportResponse;
import org.springboot.smartdining.dto.StaffActivityResponse;

import java.time.LocalDate;
import java.util.List;

/** Báo cáo của quản trị. Khoảng ngày gồm cả hai đầu; null là không giới hạn; from sau to thì 400. */
public interface AdminReportService {

    RevenueReportResponse getRevenue(LocalDate from, LocalDate to);

    /** Mỗi nhân viên đang ACTIVE hoặc có hoạt động trong khoảng, theo vai trò, họ tên, id. */
    List<StaffActivityResponse> getStaffActivity(LocalDate from, LocalDate to);
}
