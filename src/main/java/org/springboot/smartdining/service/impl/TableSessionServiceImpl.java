package org.springboot.smartdining.service.impl;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.entity.TableSession;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.mapper.DiningTableMapper;
import org.springboot.smartdining.mapper.TableSessionMapper;
import org.springboot.smartdining.security.TableTokens;
import org.springboot.smartdining.service.TableSessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springboot.smartdining.service.impl.ServiceChecks.orNow;
import static org.springboot.smartdining.service.impl.ServiceChecks.require;
import static org.springboot.smartdining.service.impl.ServiceChecks.requireReference;

@Service
@RequiredArgsConstructor
@Transactional
public class TableSessionServiceImpl implements TableSessionService {

    private final TableSessionMapper tableSessionMapper;
    private final DiningTableMapper diningTableMapper;

    @Override
    public TableSession create(TableSession input) {
        TableSession session = fromInput(input);
        session.setTokenHash(TableTokens.hash(TableTokens.newToken()));
        session.setCreatedAt(orNow(input.getCreatedAt()));
        tableSessionMapper.insert(session);
        return findExisting(session.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public TableSession getById(Long id) {
        return findExisting(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TableSession> getAll() {
        return tableSessionMapper.findAll();
    }

    @Override
    public TableSession update(Long id, TableSession input) {
        findExisting(id);
        // tokenHash để null: mapper không đụng tới cột token_hash
        TableSession session = fromInput(input);
        session.setId(id);
        tableSessionMapper.update(session);
        return findExisting(id);
    }

    @Override
    public void delete(Long id) {
        findExisting(id);
        tableSessionMapper.deleteById(id);
    }

    private TableSession findExisting(Long id) {
        TableSession session = tableSessionMapper.findById(id);
        if (session == null) {
            throw new ResourceNotFoundException("Không tìm thấy phiên bàn " + id);
        }
        return session;
    }

    private TableSession fromInput(TableSession input) {
        require(input, "dữ liệu phiên bàn");
        TableSession session = new TableSession();
        session.setTableId(requireReference(input.getTableId(), diningTableMapper::findById, "tableId"));
        session.setExpiresAt(require(input.getExpiresAt(), "expiresAt"));
        session.setClosedAt(input.getClosedAt());
        return session;
    }

}
