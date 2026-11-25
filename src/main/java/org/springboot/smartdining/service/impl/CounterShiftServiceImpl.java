package org.springboot.smartdining.service.impl;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.entity.CounterShift;
import org.springboot.smartdining.exception.BadRequestException;
import org.springboot.smartdining.exception.ConflictException;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.mapper.CounterShiftMapper;
import org.springboot.smartdining.mapper.EmployeeMapper;
import org.springboot.smartdining.service.CounterShiftService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.springboot.smartdining.service.impl.ServiceChecks.closingTime;
import static org.springboot.smartdining.service.impl.ServiceChecks.now;
import static org.springboot.smartdining.service.impl.ServiceChecks.require;
import static org.springboot.smartdining.service.impl.ServiceChecks.requireReference;

@Service
@RequiredArgsConstructor
@Transactional
public class CounterShiftServiceImpl implements CounterShiftService {

    private final CounterShiftMapper counterShiftMapper;
    private final EmployeeMapper employeeMapper;
    private final ShiftWriteLock shiftWriteLock;

    @Override
    public CounterShift create(CounterShift input) {
        shiftWriteLock.lockUntilTransactionEnds();
        require(input, "dữ liệu ca quầy");
        LocalDateTime startedAt = input.getStartedAt() != null ? input.getStartedAt() : now();
        CounterShift shift = fromInput(input, startedAt);
        shift.setStartedAt(startedAt);
        counterShiftMapper.insert(shift);
        return findExisting(shift.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public CounterShift getById(Long id) {
        return findExisting(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CounterShift> getAll() {
        return counterShiftMapper.findAll();
    }

    @Override
    public CounterShift update(Long id, CounterShift input) {
        shiftWriteLock.lockUntilTransactionEnds();
        CounterShift existing = findExisting(id);
        require(input, "dữ liệu ca quầy");
        CounterShift shift = fromInput(input, existing.getStartedAt());
        shift.setId(id);
        counterShiftMapper.update(shift);
        return findExisting(id);
    }

    @Override
    public void delete(Long id) {
        shiftWriteLock.lockUntilTransactionEnds();
        findExisting(id);
        counterShiftMapper.deleteById(id);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CounterShift close(Long id) {
        shiftWriteLock.lockUntilTransactionEnds();
        // Khóa bản ghi ca trước khi lấy giờ, để thao tác quầy đang chạy xong rồi mới chốt giờ đóng
        CounterShift shift = counterShiftMapper.findByIdForUpdate(id);
        if (shift == null) {
            throw notFound(id);
        }
        if (shift.getEndedAt() != null
                || counterShiftMapper.close(id, closingTime(shift.getStartedAt())) == 0) {
            throw new ConflictException("SHIFT_ALREADY_CLOSED", "Ca quầy đã đóng");
        }
        return findExisting(id);
    }

    private CounterShift findExisting(Long id) {
        CounterShift shift = counterShiftMapper.findById(id);
        if (shift == null) {
            throw notFound(id);
        }
        return shift;
    }

    private static ResourceNotFoundException notFound(Long id) {
        return new ResourceNotFoundException("Không tìm thấy ca quầy " + id);
    }

    private CounterShift fromInput(CounterShift input, LocalDateTime startedAt) {
        CounterShift shift = new CounterShift();
        shift.setEmployeeId(requireReference(input.getEmployeeId(), employeeMapper::findById, "employeeId"));
        if (input.getEndedAt() != null && input.getEndedAt().isBefore(startedAt)) {
            throw new BadRequestException("endedAt không được trước startedAt");
        }
        shift.setEndedAt(input.getEndedAt());
        return shift;
    }
}
