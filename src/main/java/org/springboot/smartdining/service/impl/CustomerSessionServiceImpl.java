package org.springboot.smartdining.service.impl;

import org.springboot.smartdining.dto.OpenTableSessionResponse;
import org.springboot.smartdining.entity.DiningTable;
import org.springboot.smartdining.entity.TableSession;
import org.springboot.smartdining.enums.TableStatus;
import org.springboot.smartdining.exception.ConflictException;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.exception.TableSessionInvalidException;
import org.springboot.smartdining.mapper.DiningTableMapper;
import org.springboot.smartdining.mapper.TableSessionMapper;
import org.springboot.smartdining.security.TableTokens;
import org.springboot.smartdining.service.CustomerSessionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.springboot.smartdining.service.impl.ServiceChecks.now;

@Service
@Transactional
public class CustomerSessionServiceImpl implements CustomerSessionService {

    private final TableSessionMapper tableSessionMapper;
    private final DiningTableMapper diningTableMapper;
    private final Duration timeToLive;

    public CustomerSessionServiceImpl(TableSessionMapper tableSessionMapper,
                                      DiningTableMapper diningTableMapper,
                                      @Value("${smartdining.table-session.ttl:12h}") Duration timeToLive) {
        if (timeToLive.isZero() || timeToLive.isNegative()) {
            throw new IllegalStateException("smartdining.table-session.ttl phải lớn hơn 0");
        }
        this.tableSessionMapper = tableSessionMapper;
        this.diningTableMapper = diningTableMapper;
        this.timeToLive = timeToLive;
    }

    @Override
    public OpenTableSessionResponse open(String qrToken) {
        DiningTable table = diningTableMapper.findByQrToken(qrToken);
        if (table == null) {
            throw new ResourceNotFoundException("Mã QR không hợp lệ");
        }
        requireActive(table);

        String token = TableTokens.newToken();
        LocalDateTime createdAt = now();
        TableSession session = new TableSession();
        session.setTableId(table.getId());
        session.setTokenHash(TableTokens.hash(token));
        session.setCreatedAt(createdAt);
        session.setExpiresAt(createdAt.plus(timeToLive));
        tableSessionMapper.insert(session);
        return new OpenTableSessionResponse(token, table.getId(), table.getName(), session.getExpiresAt());
    }

    @Override
    @Transactional(readOnly = true)
    public TableSession requireSession(String token) {
        if (token == null || token.isBlank()) {
            throw new TableSessionInvalidException();
        }
        TableSession session = tableSessionMapper.findByTokenHash(TableTokens.hash(token));
        if (session == null || session.getClosedAt() != null
                || !session.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new TableSessionInvalidException();
        }
        return session;
    }

    @Override
    @Transactional(readOnly = true)
    public DiningTable tableOf(String token) {
        return diningTableMapper.findById(requireSession(token).getTableId());
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public DiningTable lockTable(String token) {
        Long tableId = requireSession(token).getTableId();
        DiningTable table = diningTableMapper.findByIdForUpdate(tableId);
        // Kiểm lại: phiên có thể vừa bị đóng trong lúc chờ khóa
        requireSession(token);
        requireActive(table);
        return table;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void changeTableStatus(DiningTable table, TableStatus status) {
        if (table.getStatus() == status) {
            return;
        }
        table.setStatus(status);
        if (diningTableMapper.update(table) == 0) {
            throw new ConflictException("Bàn vừa được người khác sửa, hãy thử lại");
        }
    }

    private void requireActive(DiningTable table) {
        if (table.getStatus() == TableStatus.INACTIVE) {
            throw new ConflictException("TABLE_INACTIVE", "Bàn đã ngưng sử dụng");
        }
    }
}
