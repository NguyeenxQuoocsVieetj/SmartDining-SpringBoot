package org.springboot.smartdining.security;

import lombok.extern.slf4j.Slf4j;
import org.springboot.smartdining.entity.Employee;
import org.springboot.smartdining.enums.CodeChannel;
import org.springframework.stereotype.Component;

/**
 * Tạm thời, khi chưa gửi email, SMS thật: chỉ ghi mã ra log.
 * Ai đọc được log thì đọc được mã, nên chỉ dùng khi chạy trên máy cá nhân.
 */
@Slf4j
@Component
public class LoggingCounterCodeSender implements CounterCodeSender {

    @Override
    public void send(Employee employee, CodeChannel channel, String code) {
        log.info("Mã vào quầy của {} (kênh {}): {}", employee.getUsername(), channel, code);
    }
}
