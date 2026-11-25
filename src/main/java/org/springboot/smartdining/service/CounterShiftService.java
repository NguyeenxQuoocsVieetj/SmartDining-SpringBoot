package org.springboot.smartdining.service;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.entity.CounterShift;
import org.springboot.smartdining.exception.BadRequestException;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.mapper.CounterShiftMapper;
import org.springboot.smartdining.mapper.EmployeeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.springboot.smartdining.service.impl.ServiceChecks.now;
import static org.springboot.smartdining.service.impl.ServiceChecks.require;
import static org.springboot.smartdining.service.impl.ServiceChecks.requireReference;

@Service
@RequiredArgsConstructor
@Transactional
public class CounterShiftService {

    private final CounterShiftMapper counterShiftMapper;
    private final EmployeeMapper employeeMapper;

    public CounterShift create(CounterShift input) {
        require(input, "dữ liệu ca quầy");
        LocalDateTime startedAt = input.getStartedAt() != null ? input.getStartedAt() : now();
        CounterShift shift = fromInput(input, startedAt);
        shift.setStartedAt(startedAt);
        counterShiftMapper.insert(shift);
        return findExisting(shift.getId());
    }

    @Transactional(readOnly = true)
    public CounterShift getById(Long id) {
        return findExisting(id);
    }

    @Transactional(readOnly = true)
    public List<CounterShift> getAll() {
        return counterShiftMapper.findAll();
    }

    public CounterShift update(Long id, CounterShift input) {
        CounterShift existing = findExisting(id);
        require(input, "dữ liệu ca quầy");
        CounterShift shift = fromInput(input, existing.getStartedAt());
        shift.setId(id);
        counterShiftMapper.update(shift);
        return findExisting(id);
    }

    public void delete(Long id) {
        findExisting(id);
        counterShiftMapper.deleteById(id);
    }

    private CounterShift findExisting(Long id) {
        CounterShift shift = counterShiftMapper.findById(id);
        if (shift == null) {
            throw new ResourceNotFoundException("Không tìm thấy ca quầy " + id);
        }
        return shift;
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
