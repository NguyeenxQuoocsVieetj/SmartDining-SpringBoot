package org.springboot.smartdining.service;

import org.springboot.smartdining.dto.OverviewResponse;

import java.util.List;

/** Tổng quan trong ngày của quản trị (ADM-1). */
public interface AdminDashboardService {

    OverviewResponse getOverview();

    /**
     * Danh sách chi tiết của một ô trên trang tổng quan.
     *
     * @param type paid, open, waiting, employees hoặc requests; loại khác thì 400
     */
    List<?> getDetails(String type);
}
