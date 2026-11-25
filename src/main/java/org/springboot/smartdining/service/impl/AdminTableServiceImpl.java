package org.springboot.smartdining.service.impl;

import org.springboot.smartdining.dto.TableQrResponse;
import org.springboot.smartdining.entity.DiningTable;
import org.springboot.smartdining.enums.TableStatus;
import org.springboot.smartdining.exception.ConflictException;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.mapper.DiningTableMapper;
import org.springboot.smartdining.mapper.TableSessionMapper;
import org.springboot.smartdining.service.AdminTableService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import static org.springboot.smartdining.service.impl.ServiceChecks.now;

@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
public class AdminTableServiceImpl implements AdminTableService {

    private final DiningTableMapper diningTableMapper;
    private final TableSessionMapper tableSessionMapper;
    private final String qrBaseUrl;

    public AdminTableServiceImpl(DiningTableMapper diningTableMapper,
                                 TableSessionMapper tableSessionMapper,
                                 @Value("${smartdining.qr.base-url:http://localhost:8080/?qr=}") String qrBaseUrl) {
        this.diningTableMapper = diningTableMapper;
        this.tableSessionMapper = tableSessionMapper;
        this.qrBaseUrl = qrBaseUrl;
    }

    @Override
    public DiningTable activate(Long id) {
        DiningTable table = lock(id);
        if (table.getStatus() != TableStatus.INACTIVE) {
            throw new ConflictException("INVALID_STATUS_TRANSITION", "Chỉ kích hoạt được bàn đã ngưng");
        }
        table.setStatus(TableStatus.AVAILABLE);
        save(table);
        return diningTableMapper.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public TableQrResponse getQr(Long id) {
        return toResponse(require(diningTableMapper.findById(id), id));
    }

    @Override
    public TableQrResponse regenerateQr(Long id) {
        DiningTable table = lock(id);
        table.setQrToken(QrTokens.newToken());
        save(table);
        tableSessionMapper.closeOpenByTableId(id, now());
        return toResponse(table);
    }

    private DiningTable lock(Long id) {
        return require(diningTableMapper.findByIdForUpdate(id), id);
    }

    private static DiningTable require(DiningTable table, Long id) {
        if (table == null) {
            throw new ResourceNotFoundException("Không tìm thấy bàn " + id);
        }
        return table;
    }

    private void save(DiningTable table) {
        if (diningTableMapper.update(table) == 0) {
            throw new ConflictException("Bàn vừa được người khác sửa, hãy thử lại");
        }
    }

    private TableQrResponse toResponse(DiningTable table) {
        return new TableQrResponse(table.getId(), table.getName(), table.getQrToken(),
                qrBaseUrl + table.getQrToken(), table.getVersion());
    }
}
