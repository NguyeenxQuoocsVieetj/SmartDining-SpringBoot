package org.springboot.smartdining.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/** Phản hồi dùng chung cho các controller. */
final class Responses {

    private Responses() {
    }

    /** 201 kèm header Location trỏ tới bản ghi vừa tạo. */
    static <T> ResponseEntity<T> created(Object id, T body) {
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).body(body);
    }
}
