package org.springboot.smartdining.security;

import org.springboot.smartdining.entity.Employee;
import org.springboot.smartdining.enums.CodeChannel;

/** Đưa mã vào quầy tới nhân viên. Thay bản cài đặt khác khi gửi email, SMS thật. */
public interface CounterCodeSender {

    void send(Employee employee, CodeChannel channel, String code);
}
