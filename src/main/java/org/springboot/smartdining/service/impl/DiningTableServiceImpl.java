package org.springboot.smartdining.service.impl;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.entity.DiningTable;
import org.springboot.smartdining.enums.TableStatus;
import org.springboot.smartdining.exception.ConflictException;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.mapper.DiningTableMapper;
import org.springboot.smartdining.mapper.OrderMapper;
import org.springboot.smartdining.service.DiningTableService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;

import static org.springboot.smartdining.service.impl.ServiceChecks.require;
import static org.springboot.smartdining.service.impl.ServiceChecks.requireText;

@Service
@RequiredArgsConstructor
@Transactional
public class DiningTableServiceImpl implements DiningTableService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final DiningTableMapper diningTableMapper;
    private final OrderMapper orderMapper;

    @Override
    public DiningTable create(DiningTable input) {
        DiningTable table = fromInput(input);
        table.setQrToken(newQrToken());
        diningTableMapper.insert(table);
        return findExisting(table.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public DiningTable getById(Long id) {
        return findExisting(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiningTable> getAll() {
        return diningTableMapper.findAll();
    }

    @Override
    public DiningTable update(Long id, DiningTable input) {
        DiningTable existing = findExisting(id);
        DiningTable table = fromInput(input);
        table.setId(id);
        table.setVersion(require(input.getVersion(), "version"));
        table.setQrToken(existing.getQrToken());

        boolean renamed = !existing.getName().equals(table.getName());
        if (renamed && existing.getStatus() != TableStatus.AVAILABLE && existing.getStatus() != TableStatus.INACTIVE) {
            throw new ConflictException("Chỉ đổi tên bàn khi bàn đang trống hoặc đã ngưng");
        }
        if (table.getStatus() == TableStatus.INACTIVE) {
            requireNoOpenOrder(id);
        }
        save(table);
        return findExisting(id);
    }

    @Override
    public void delete(Long id) {
        DiningTable existing = findExisting(id);
        requireNoOpenOrder(id);
        existing.setStatus(TableStatus.INACTIVE);
        save(existing);
    }

    private DiningTable findExisting(Long id) {
        DiningTable table = diningTableMapper.findById(id);
        if (table == null) {
            throw new ResourceNotFoundException("Không tìm thấy bàn " + id);
        }
        return table;
    }

    private void save(DiningTable table) {
        if (diningTableMapper.update(table) == 0) {
            findExisting(table.getId());
            throw new ConflictException("Bàn đã được người khác sửa, hãy tải lại");
        }
    }

    private void requireNoOpenOrder(Long tableId) {
        if (orderMapper.countUnpaidByTableId(tableId) > 0) {
            throw new ConflictException("Bàn còn đơn chưa thanh toán");
        }
    }

    private DiningTable fromInput(DiningTable input) {
        require(input, "dữ liệu bàn");
        DiningTable table = new DiningTable();
        table.setName(requireText(input.getName(), "name", 50));
        table.setStatus(input.getStatus());
        return table;
    }

    /** 16 byte ngẫu nhiên viết dạng hex, đủ 32 ký tự. */
    private static String newQrToken() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
