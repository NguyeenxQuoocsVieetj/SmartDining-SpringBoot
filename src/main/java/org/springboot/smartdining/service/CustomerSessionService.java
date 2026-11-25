package org.springboot.smartdining.service;

import org.springboot.smartdining.dto.OpenTableSessionResponse;
import org.springboot.smartdining.entity.DiningTable;
import org.springboot.smartdining.entity.TableSession;
import org.springboot.smartdining.enums.TableStatus;

/** Phiên bàn của khách: mở bằng mã QR, xác định bàn bằng token. */
public interface CustomerSessionService {

    /** Mỗi lần quét tạo một phiên riêng, nên nhiều điện thoại dùng chung một bàn được. */
    OpenTableSessionResponse open(String qrToken);

    /** Phiên còn dùng được ứng với token, không thì ném lỗi 401. */
    TableSession requireSession(String token);

    /** Bàn của phiên, dùng cho thao tác chỉ đọc. */
    DiningTable tableOf(String token);

    /**
     * Khóa bản ghi bàn của phiên tới hết transaction, để mọi thao tác đổi giỏ, đơn, bàn
     * trên cùng một bàn chạy lần lượt. Phải gọi trước khi đọc đơn, và gọi bên trong một transaction.
     * Transaction gọi hàm này dùng READ_COMMITTED để đọc được dữ liệu mới nhất sau khi chờ khóa.
     */
    DiningTable lockTable(String token);

    /** Đổi trạng thái bàn đã khóa; không làm gì nếu bàn đang ở trạng thái đó. */
    void changeTableStatus(DiningTable table, TableStatus status);
}
