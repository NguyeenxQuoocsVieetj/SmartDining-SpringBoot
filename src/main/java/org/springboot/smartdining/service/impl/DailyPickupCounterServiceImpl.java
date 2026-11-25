package org.springboot.smartdining.service.impl;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.entity.DailyPickupCounter;
import org.springboot.smartdining.exception.BadRequestException;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.mapper.DailyPickupCounterMapper;
import org.springboot.smartdining.service.DailyPickupCounterService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.springboot.smartdining.service.impl.ServiceChecks.require;

@Service
@RequiredArgsConstructor
@Transactional
public class DailyPickupCounterServiceImpl implements DailyPickupCounterService {

    private final DailyPickupCounterMapper dailyPickupCounterMapper;

    @Override
    public DailyPickupCounter create(DailyPickupCounter input) {
        require(input, "dữ liệu bộ đếm");
        LocalDate businessDate = require(input.getBusinessDate(), "businessDate");
        dailyPickupCounterMapper.insert(new DailyPickupCounter(businessDate, requireLastCode(input)));
        return findExisting(businessDate);
    }

    @Override
    @Transactional(readOnly = true)
    public DailyPickupCounter getById(LocalDate businessDate) {
        return findExisting(businessDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyPickupCounter> getAll() {
        return dailyPickupCounterMapper.findAll();
    }

    @Override
    public DailyPickupCounter update(LocalDate businessDate, DailyPickupCounter input) {
        findExisting(businessDate);
        require(input, "dữ liệu bộ đếm");
        dailyPickupCounterMapper.update(new DailyPickupCounter(businessDate, requireLastCode(input)));
        return findExisting(businessDate);
    }

    @Override
    public void delete(LocalDate businessDate) {
        findExisting(businessDate);
        dailyPickupCounterMapper.deleteById(businessDate);
    }

    @Override
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
