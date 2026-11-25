package org.springboot.smartdining.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springboot.smartdining.dto.AddCartItemRequest;
import org.springboot.smartdining.dto.TableOrderResponse;
import org.springboot.smartdining.dto.UpdateCartItemRequest;
import org.springboot.smartdining.security.TableTokens;
import org.springboot.smartdining.service.CustomerOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Giỏ và đơn của khách tại bàn. */
@RestController
@RequestMapping("/api/v1/table-session/order")
@RequiredArgsConstructor
public class TableOrderController {

    private final CustomerOrderService customerOrderService;

    @GetMapping
    public TableOrderResponse getOrder(@RequestHeader(TableTokens.HEADER) String token) {
        return customerOrderService.getOrder(token);
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public TableOrderResponse addItem(@RequestHeader(TableTokens.HEADER) String token,
                                      @Valid @RequestBody AddCartItemRequest request) {
        return customerOrderService.addItem(token, request.productId(), request.quantity(), request.note());
    }

    @PatchMapping("/items/{itemId}")
    public TableOrderResponse updateQuantity(@RequestHeader(TableTokens.HEADER) String token,
                                             @PathVariable Long itemId,
                                             @Valid @RequestBody UpdateCartItemRequest request) {
        return customerOrderService.updateQuantity(token, itemId, request.quantity());
    }

    /** Gửi bếp. */
    @PostMapping("/submit")
    public TableOrderResponse submit(@RequestHeader(TableTokens.HEADER) String token) {
        return customerOrderService.submit(token);
    }

    @DeleteMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(@RequestHeader(TableTokens.HEADER) String token, @PathVariable Long itemId) {
        customerOrderService.removeItem(token, itemId);
    }
}
