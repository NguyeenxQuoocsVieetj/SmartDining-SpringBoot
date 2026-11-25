package org.springboot.smartdining.service;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.springboot.smartdining.service.ServiceChecks.now;

/** Phiên bàn của khách: mở bằng mã QR, xác định bàn bằng token. */
@Service
@Transactional
public class CustomerSessionService {

    private final TableSessionMapper tableSessionMapper;
    private final DiningTableMapper diningTableMapper;
    private final Duration timeToLive;

    public CustomerSessionService(TableSessionMapper tableSessionMapper,
                                  DiningTableMapper diningTableMapper,
                                  @Value("${smartdining.table-session.ttl:12h}") Duration timeToLive) {
        if (timeToLive.isZero() || timeToLive.isNegative()) {
            throw new IllegalStateException("smartdining.table-session.ttl phải lớn hơn 0");
        }
        this.tableSessionMapper = tableSessionMapper;
        this.diningTableMapper = diningTableMapper;
        this.timeToLive = timeToLive;
    }

    /** Mỗi lần quét tạo một phiên riêng, nên nhiều điện thoại dùng chung một bàn được. */
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

    /** Phiên còn dùng được ứng với token, không thì ném lỗi 401. */
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

    /** Bàn của phiên, dùng cho thao tác chỉ đọc. */
    @Transactional(readOnly = true)
    public DiningTable tableOf(String token) {
        return diningTableMapper.findById(requireSession(token).getTableId());
    }

    /**
     * Khóa bản ghi bàn của phiên tới hết transaction, để mọi thao tác đổi giỏ, đơn, bàn
     * trên cùng một bàn chạy lần lượt. Phải gọi trước khi đọc đơn.
     * Transaction gọi hàm này dùng READ_COMMITTED để đọc được dữ liệu mới nhất sau khi chờ khóa.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public DiningTable lockTable(String token) {
        Long tableId = requireSession(token).getTableId();
        DiningTable table = diningTableMapper.findByIdForUpdate(tableId);
        // Kiểm lại: phiên có thể vừa bị đóng trong lúc chờ khóa
        requireSession(token);
        requireActive(table);
        return table;
    }

    /** Đổi trạng thái bàn đã khóa; không làm gì nếu bàn đang ở trạng thái đó. */
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
