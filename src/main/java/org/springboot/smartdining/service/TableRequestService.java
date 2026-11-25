package org.springboot.smartdining.service;

import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.entity.TableRequest;
import org.springboot.smartdining.enums.TableRequestStatus;
import org.springboot.smartdining.enums.TableRequestType;
import org.springboot.smartdining.exception.ResourceNotFoundException;
import org.springboot.smartdining.mapper.DiningTableMapper;
import org.springboot.smartdining.mapper.TableRequestMapper;
import org.springboot.smartdining.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springboot.smartdining.service.ServiceChecks.now;
import static org.springboot.smartdining.service.ServiceChecks.optionalText;
import static org.springboot.smartdining.service.ServiceChecks.orNow;
import static org.springboot.smartdining.service.ServiceChecks.require;
import static org.springboot.smartdining.service.ServiceChecks.requireReference;
import static org.springboot.smartdining.service.ServiceChecks.requireText;

@Service
@RequiredArgsConstructor
@Transactional
public class TableRequestService {

    private final TableRequestMapper tableRequestMapper;
    private final DiningTableMapper diningTableMapper;
    private final CurrentUser currentUser;

    public TableRequest create(TableRequest input) {
        TableRequest request = fromInput(input);
        request.setRequestedAt(orNow(input.getRequestedAt()));
        fillCompletion(request, request.getStatus());
        tableRequestMapper.insert(request);
        return findExisting(request.getId());
    }

    @Transactional(readOnly = true)
    public TableRequest getById(Long id) {
        return findExisting(id);
    }

    @Transactional(readOnly = true)
    public List<TableRequest> getAll() {
        return tableRequestMapper.findAll();
    }

    public TableRequest update(Long id, TableRequest input) {
        TableRequest existing = findExisting(id);
        TableRequest request = fromInput(input);
        request.setId(id);
        request.setCompletedBy(existing.getCompletedBy());
        request.setCompletedAt(existing.getCompletedAt());
        fillCompletion(request, request.getStatus() != null ? request.getStatus() : existing.getStatus());
        tableRequestMapper.update(request);
        return findExisting(id);
    }

    public void delete(Long id) {
        findExisting(id);
        tableRequestMapper.deleteById(id);
    }

    private TableRequest findExisting(Long id) {
        TableRequest request = tableRequestMapper.findById(id);
        if (request == null) {
            throw new ResourceNotFoundException("Không tìm thấy yêu cầu " + id);
        }
        return request;
    }

    private void fillCompletion(TableRequest request, TableRequestStatus status) {
        if (status != TableRequestStatus.COMPLETED) {
            return;
        }
        if (request.getCompletedBy() == null) {
            request.setCompletedBy(currentUser.getId());
        }
        if (request.getCompletedAt() == null) {
            request.setCompletedAt(now());
        }
    }

    private TableRequest fromInput(TableRequest input) {
        require(input, "dữ liệu yêu cầu");
        TableRequest request = new TableRequest();
        request.setTableId(requireReference(input.getTableId(), diningTableMapper::findById, "tableId"));
        request.setRequestType(require(input.getRequestType(), "requestType"));
        request.setServiceType(request.getRequestType() == TableRequestType.SERVICE
                ? requireText(input.getServiceType(), "serviceType cho yêu cầu phục vụ", 80)
                : optionalText(input.getServiceType(), "serviceType", 80));
        request.setNote(optionalText(input.getNote(), "note", 255));
        request.setStatus(input.getStatus());
        return request;
    }
}
