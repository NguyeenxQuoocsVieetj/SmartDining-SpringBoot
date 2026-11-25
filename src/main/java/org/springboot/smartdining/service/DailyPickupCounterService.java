package org.springboot.smartdining.service;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.entity.DailyPickupCounter;
import org.springboot.smartdining.exception.BadRequestException;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.mapper.DailyPickupCounterMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.springboot.smartdining.service.ServiceChecks.require;

@Service
@RequiredArgsConstructor
@Transactional
public class DailyPickupCounterService {

    private final DailyPickupCounterMapper dailyPickupCounterMapper;

    public DailyPickupCounter create(DailyPickupCounter input) {
        require(input, "dữ liệu bộ đếm");
        LocalDate businessDate = require(input.getBusinessDate(), "businessDate");
        dailyPickupCounterMapper.insert(new DailyPickupCounter(businessDate, requireLastCode(input)));
        return findExisting(businessDate);
    }

    @Transactional(readOnly = true)
    public DailyPickupCounter getById(LocalDate businessDate) {
        return findExisting(businessDate);
    }

    @Transactional(readOnly = true)
    public List<DailyPickupCounter> getAll() {
        return dailyPickupCounterMapper.findAll();
    }

    public DailyPickupCounter update(LocalDate businessDate, DailyPickupCounter input) {
        findExisting(businessDate);
        require(input, "dữ liệu bộ đếm");
        dailyPickupCounterMapper.update(new DailyPickupCounter(businessDate, requireLastCode(input)));
        return findExisting(businessDate);
    }

    public void delete(LocalDate businessDate) {
        findExisting(businessDate);
        dailyPickupCounterMapper.deleteById(businessDate);
    }

    /**
     * Cấp mã lấy đồ tiếp theo của ngày, an toàn khi nhiều người tạo cùng lúc.
     * Hai câu lệnh phải chạy trên cùng kết nối, nên bắt buộc nằm trong một transaction.
     */
    public int nextCode(LocalDate businessDate) {
        require(businessDate, "businessDate");
        dailyPickupCounterMapper.upsertNextCode(businessDate);
        return dailyPickupCounterMapper.selectLastInsertId();
    }

    private DailyPickupCounter findExisting(LocalDate businessDate) {
        DailyPickupCounter counter = dailyPickupCounterMapper.findById(businessDate);
        if (counter == null) {
            throw new ResourceNotFoundException("Không tìm thấy bộ đếm ngày " + businessDate);
        }
        return counter;
    }

    private int requireLastCode(DailyPickupCounter input) {
        int lastCode = require(input.getLastCode(), "lastCode");
        if (lastCode < 0) {
            throw new BadRequestException("lastCode không được âm");
        }
        return lastCode;
    }
}
