package org.springboot.smartdining.security;

/** Che email, số điện thoại trước khi trả ra ngoài. */
public final class ContactMasks {

    private static final String HIDDEN = "***";
    private static final int VISIBLE_PHONE_DIGITS = 3;

    private ContactMasks() {
    }

    /** Giữ ký tự đầu và tên miền: s***@example.com. */
    public static String email(String email) {
        if (email == null) {
            return null;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return HIDDEN;
        }
        // Tên chỉ có một ký tự thì che hết
        String first = at > 1 ? email.substring(0, 1) : "";
        return first + HIDDEN + email.substring(at);
    }

    /** Giữ ba số cuối: *******001. Không có số thì trả null. */
    public static String phone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        String value = phone.strip();
        if (value.length() <= VISIBLE_PHONE_DIGITS) {
            return HIDDEN;
        }
        return "*".repeat(value.length() - VISIBLE_PHONE_DIGITS)
                + value.substring(value.length() - VISIBLE_PHONE_DIGITS);
    }
}
