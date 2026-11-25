package org.springboot.smartdining.service.impl;

import org.springboot.smartdining.exception.BadRequestException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;

/** Kiểm tra dữ liệu đầu vào dùng chung cho các service. */
final class ServiceChecks {

    /** Giá trị lớn nhất của cột DECIMAL(13,2). */
    private static final BigDecimal MAX_MONEY = new BigDecimal("99999999999.99");

    private ServiceChecks() {
    }

    static <T> T require(T value, String field) {
        if (value == null) {
            throw new BadRequestException("Thiếu " + field);
        }
        return value;
    }

    static String requireText(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("Thiếu " + field);
        }
        return optionalText(value, field, maxLength);
    }

    static String optionalText(String value, String field, int maxLength) {
        if (value != null && value.codePointCount(0, value.length()) > maxLength) {
            throw new BadRequestException(field + " dài quá " + maxLength + " ký tự");
        }
        return value;
    }

    /** Số tiền từ 0, tối đa 2 chữ số thập phân. Cho phép null. */
    static BigDecimal optionalMoney(BigDecimal value, String field) {
        if (value == null) {
            return null;
        }
        if (value.signum() < 0) {
            throw new BadRequestException(field + " không được âm");
        }
        if (value.compareTo(MAX_MONEY) > 0) {
            throw new BadRequestException(field + " quá lớn");
        }
        if (value.stripTrailingZeros().scale() > 2) {
            throw new BadRequestException(field + " chỉ được tối đa 2 chữ số thập phân");
        }
        return value;
    }

    static BigDecimal requireMoney(BigDecimal value, String field) {
        return optionalMoney(require(value, field), field);
    }

    static Long optionalReference(Long id, Function<Long, ?> finder, String field) {
        if (id != null && finder.apply(id) == null) {
            throw new BadRequestException(field + " không tồn tại: " + id);
        }
        return id;
    }

    static Long requireReference(Long id, Function<Long, ?> finder, String field) {
        return optionalReference(require(id, field), finder, field);
    }

    /** Giờ hiện tại, bỏ phần lẻ giây cho khớp cột DATETIME. */
    static LocalDateTime now() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    /** Giờ đóng ca: giờ hiện tại, nhưng không sớm hơn giờ bắt đầu (database bắt buộc). */
    static LocalDateTime closingTime(LocalDateTime startedAt) {
        LocalDateTime current = now();
        return current.isBefore(startedAt) ? startedAt : current;
    }

    /**
     * Mốc tạo bản ghi: dùng giá trị được đưa vào, không có thì lấy giờ ứng dụng.
     * Không để database tự điền CURRENT_TIMESTAMP vì múi giờ của nó có thể khác (ví dụ UTC).
     */
    static LocalDateTime orNow(LocalDateTime value) {
        return value != null ? value : now();
    }
}
