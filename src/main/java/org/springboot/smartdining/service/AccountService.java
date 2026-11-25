package org.springboot.smartdining.service;

import org.springboot.smartdining.dto.RegisterRequest;
import org.springboot.smartdining.entity.Employee;

/** Đăng ký và duyệt tài khoản nhân viên. */
public interface AccountService {

    /** Tạo tài khoản STAFF đang chờ duyệt (ACC-1); trùng username thì 409 USERNAME_TAKEN. */
    Employee register(RegisterRequest request);

    /** Duyệt tài khoản đang chờ: thành STAFF đang hoạt động. */
    Employee approve(Long id);
}
