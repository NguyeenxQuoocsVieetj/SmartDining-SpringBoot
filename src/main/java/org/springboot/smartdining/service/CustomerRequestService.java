package org.springboot.smartdining.service;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.CustomerRequestResponse;
import org.springboot.smartdining.entity.DiningTable;
import org.springboot.smartdining.entity.TableRequest;
import org.springboot.smartdining.enums.TableRequestStatus;
import org.springboot.smartdining.enums.TableRequestType;
import org.springboot.smartdining.enums.TableStatus;
import org.springboot.smartdining.exception.ConflictException;
import org.springboot.smartdining.mapper.OrderMapper;
import org.springboot.smartdining.mapper.TableRequestMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import static org.springboot.smartdining.service.ServiceChecks.now;
import static org.springboot.smartdining.service.ServiceChecks.optionalText;
import static org.springboot.smartdining.service.ServiceChecks.requireText;

/** Khách tại bàn yêu cầu thanh toán hoặc gọi nhân viên. */
@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class CustomerRequestService {

    private final CustomerSessionService customerSessionService;
    private final OrderMapper orderMapper;
    private final TableRequestMapper tableRequestMapper;

    /** @param created false khi bàn đã có sẵn yêu cầu thanh toán đang chờ */
    public record PaymentRequestResult(CustomerRequestResponse request, boolean created) {
    }

    public PaymentRequestResult requestPayment(String token) {
        DiningTable table = customerSessionService.lockTable(token);
        if (orderMapper.findUnpaidByTableId(table.getId()) == null) {
            throw new ConflictException("NO_OPEN_ORDER", "Bàn chưa có đơn để thanh toán");
        }
        customerSessionService.changeTableStatus(table, TableStatus.WAITING_PAYMENT);

        TableRequest pending = tableRequestMapper.findPendingPaymentByTableId(table.getId());
        if (pending != null) {
            return new PaymentRequestResult(CustomerRequestResponse.from(pending), false);
        }
        TableRequest request = newRequest(table, TableRequestType.PAYMENT);
        tableRequestMapper.insert(request);
        return new PaymentRequestResult(CustomerRequestResponse.from(request), true);
    }

    /** Gọi nhân viên, được gửi nhiều lần. Bàn đang trống thì từ chối. */
    public CustomerRequestResponse callService(String token, String serviceType, String note) {
        DiningTable table = customerSessionService.lockTable(token);
        if (table.getStatus() == TableStatus.AVAILABLE) {
            throw new ConflictException("TABLE_NOT_OCCUPIED", "Bàn chưa gọi món nên chưa gọi nhân viên được");
        }
        TableRequest request = newRequest(table, TableRequestType.SERVICE);
        request.setServiceType(requireText(serviceType == null ? null : serviceType.strip(), "serviceType", 80));
        request.setNote(optionalText(note == null || note.isBlank() ? null : note.strip(), "note", 255));
        tableRequestMapper.insert(request);
        return CustomerRequestResponse.from(request);
    }

    private TableRequest newRequest(DiningTable table, TableRequestType type) {
        TableRequest request = new TableRequest();
        request.setTableId(table.getId());
        request.setRequestType(type);
        request.setStatus(TableRequestStatus.PENDING);
        request.setRequestedAt(now());
        return request;
    }
}
