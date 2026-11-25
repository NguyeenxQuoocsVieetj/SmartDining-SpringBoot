package org.springboot.smartdining.service;

import org.springboot.smartdining.entity.TableRequest;

import java.util.List;

public interface TableRequestService {

    TableRequest create(TableRequest input);

    TableRequest getById(Long id);

    List<TableRequest> getAll();

    TableRequest update(Long id, TableRequest input);

    void delete(Long id);
}
