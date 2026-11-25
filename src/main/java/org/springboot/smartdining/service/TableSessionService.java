package org.springboot.smartdining.service;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.entity.TableSession;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.mapper.DiningTableMapper;
import org.springboot.smartdining.mapper.TableSessionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;

import static org.springboot.smartdining.service.ServiceChecks.require;
import static org.springboot.smartdining.service.ServiceChecks.requireReference;

@Service
@RequiredArgsConstructor
@Transactional
public class TableSessionService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final TableSessionMapper tableSessionMapper;
    private final DiningTableMapper diningTableMapper;

    public TableSession create(TableSession input) {
        TableSession session = fromInput(input);
        session.setTokenHash(newTokenHash());
        session.setCreatedAt(input.getCreatedAt());
        tableSessionMapper.insert(session);
        return findExisting(session.getId());
    }

    @Transactional(readOnly = true)
    public TableSession getById(Long id) {
        return findExisting(id);
    }

    @Transactional(readOnly = true)
    public List<TableSession> getAll() {
        return tableSessionMapper.findAll();
    }

    public TableSession update(Long id, TableSession input) {
        findExisting(id);
        // tokenHash để null: mapper không đụng tới cột token_hash
        TableSession session = fromInput(input);
        session.setId(id);
        tableSessionMapper.update(session);
        return findExisting(id);
    }

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

    /**
     * SHA-256 của một chuỗi ngẫu nhiên. Chuỗi gốc chưa được trả cho ai;
     * luồng khách quét QR sẽ cần trả nó về khi làm tới.
     */
    private static String newTokenHash() {
        byte[] token = new byte[32];
        RANDOM.nextBytes(token);
        String rawToken = HexFormat.of().formatHex(token);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Máy ảo Java không hỗ trợ SHA-256", e);
        }
    }
}
