package org.springboot.smartdining.service.impl;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.CounterEmployeeResponse;
import org.springboot.smartdining.dto.CounterSessionResponse;
import org.springboot.smartdining.dto.SendCodeResponse;
import org.springboot.smartdining.entity.CounterShift;
import org.springboot.smartdining.entity.Employee;
import org.springboot.smartdining.enums.CodeChannel;
import org.springboot.smartdining.enums.EmployeeRole;
import org.springboot.smartdining.enums.EmployeeStatus;
import org.springboot.smartdining.exception.BadRequestException;
import org.springboot.smartdining.exception.ConflictException;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.mapper.CounterShiftMapper;
import org.springboot.smartdining.mapper.EmployeeMapper;
import org.springboot.smartdining.security.ContactMasks;
import org.springboot.smartdining.security.CounterCodeSender;
import org.springboot.smartdining.security.CounterCodeStore;
import org.springboot.smartdining.service.CounterService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.springboot.smartdining.service.impl.ServiceChecks.closingTime;
import static org.springboot.smartdining.service.impl.ServiceChecks.now;
import static org.springboot.smartdining.service.impl.ServiceChecks.require;
import static org.springboot.smartdining.service.impl.ServiceChecks.requireText;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CounterServiceImpl implements CounterService {

    private final CounterShiftMapper counterShiftMapper;
    private final EmployeeMapper employeeMapper;
    private final CounterCodeStore codeStore;
    private final CounterCodeSender codeSender;

    @Override
    public CounterSessionResponse getCurrentShift() {
        CounterShift shift = requireOpenShift();
        return CounterSessionResponse.from(shift, employeeMapper.findById(shift.getEmployeeId()));
    }

    @Override
    public CounterEmployeeResponse findCandidate(String username) {
        String name = cleanUsername(username);
        requireNoOpenShift();
        Employee employee = requireCandidate(name);
        return new CounterEmployeeResponse(employee.getId(), employee.getUsername(), employee.getFullName(),
                ContactMasks.email(employee.getEmail()), ContactMasks.phone(employee.getPhone()));
    }

    @Override
    public SendCodeResponse sendCode(String username, CodeChannel channel) {
        String name = cleanUsername(username);
        require(channel, "channel");
        requireNoOpenShift();
        Employee employee = requireCandidate(name);
        String destination = channel == CodeChannel.EMAIL
                ? ContactMasks.email(employee.getEmail()) : ContactMasks.phone(employee.getPhone());
        if (destination == null) {
            throw new ConflictException("CHANNEL_UNAVAILABLE", "Nhân viên chưa có thông tin cho kênh " + channel);
        }
        CounterCodeStore.IssuedCode issued = codeStore.issue(employee.getId());
        codeSender.send(employee, channel, issued.code());
        return new SendCodeResponse(employee.getUsername(), channel, destination, issued.expiresAt(),
                codeStore.resendDelay().toSeconds());
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CounterSessionResponse openShift(String username, String code) {
        String name = cleanUsername(username);
        require(code, "code");
        requireNoOpenShift();
        Employee employee = requireCandidate(name);
        switch (codeStore.verify(employee.getId(), code)) {
            case WRONG -> throw new BadRequestException("INVALID_CODE", "Mã không đúng");
            case UNAVAILABLE -> throw new ConflictException("CODE_EXPIRED",
                    "Mã chưa được gửi, đã hết hạn hoặc đã bị hủy; hãy gửi mã mới");
            case ACCEPTED -> {
                // mã đúng, mở ca
            }
        }

        CounterShift shift = new CounterShift();
        shift.setEmployeeId(employee.getId());
        shift.setStartedAt(now());
        insertOpenShift(shift);
        clearCodesAfterCommit();
        return CounterSessionResponse.from(shift, employee);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void closeCurrentShift() {
        CounterShift shift = requireOpenShift();
        if (counterShiftMapper.close(shift.getId(), closingTime(shift.getStartedAt())) == 0) {
            throw noOpenShift();
        }
    }

    /** Người khác vừa mở ca trước thì ràng buộc "một ca mở" chặn lại. */
    private void insertOpenShift(CounterShift shift) {
        try {
            counterShiftMapper.insert(shift);
        } catch (DataIntegrityViolationException e) {
            if (counterShiftMapper.findOpen() != null) {
                throw shiftAlreadyOpen();
            }
            throw e;
        }
    }

    /** Chỉ bỏ mã khi ca đã thật sự được lưu. */
    private void clearCodesAfterCommit() {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                codeStore.clear();
            }
        });
    }

    private CounterShift requireOpenShift() {
        CounterShift shift = counterShiftMapper.findOpen();
        if (shift == null) {
            throw noOpenShift();
        }
        return shift;
    }

    private static ResourceNotFoundException noOpenShift() {
        return new ResourceNotFoundException("NO_OPEN_SHIFT", "Chưa có ca quầy nào đang mở");
    }

    private void requireNoOpenShift() {
        if (counterShiftMapper.findOpen() != null) {
            throw shiftAlreadyOpen();
        }
    }

    private Employee requireCandidate(String username) {
        Employee employee = employeeMapper.findByUsername(username);
        if (employee == null || employee.getRole() != EmployeeRole.STAFF
                || employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new ResourceNotFoundException("Không tìm thấy nhân viên đủ điều kiện trực quầy");
        }
        return employee;
    }

    private static String cleanUsername(String username) {
        return requireText(require(username, "username").strip(), "username", 50);
    }

    private static ConflictException shiftAlreadyOpen() {
        return new ConflictException("SHIFT_ALREADY_OPEN", "Đang có ca quầy mở");
    }
}
