package org.springboot.smartdining.dto;

/** Mã QR của bàn. Giao diện tự vẽ ảnh QR từ url. */
public record TableQrResponse(
        Long tableId,
        String tableName,
        String qrToken,
        String url,
        Integer version) {
}
