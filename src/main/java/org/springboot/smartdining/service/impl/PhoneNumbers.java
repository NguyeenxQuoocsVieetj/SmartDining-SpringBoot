package org.springboot.smartdining.service.impl;

/** Chuẩn hóa số điện thoại trước khi lưu và tra cứu. */
final class PhoneNumbers {

    private PhoneNumbers() {
    }

    /** Bỏ khoảng trắng, dấu chấm, gạch; đổi đầu +84 thành 0. */
    static String normalize(String phone) {
        if (phone == null) {
            return null;
        }
        String digits = phone.replaceAll("[\\s\\u00A0.\\-]", "");
        return digits.startsWith("+84") ? "0" + digits.substring(3) : digits;
    }
}
