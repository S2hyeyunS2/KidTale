package com.hyeyuns2.kidtale.order.controller;

import com.hyeyuns2.kidtale.common.response.ApiResponse;
import com.hyeyuns2.kidtale.common.security.SecurityUtils;
import com.hyeyuns2.kidtale.order.dto.request.OrderCreateRequest;
import com.hyeyuns2.kidtale.order.dto.response.OrderResponse;
import com.hyeyuns2.kidtale.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final SecurityUtils securityUtils;

    public OrderController(OrderService orderService, SecurityUtils securityUtils) {
        this.orderService = orderService;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody OrderCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.debug("[OrderController] POST /api/orders. storyId={}", request.storyId());
        Long userId = securityUtils.resolveUserIdOrThrow(userDetails);
        OrderResponse response = orderService.createOrder(request, userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("주문이 완료되었습니다.", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable("id") Long id) {
        log.debug("[OrderController] GET /api/orders/{}", id);
        OrderResponse response = orderService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByStoryId(
            @RequestParam(name = "storyId") Long storyId
    ) {
        log.debug("[OrderController] GET /api/orders?storyId={}", storyId);
        OrderResponse response = orderService.findByStoryId(storyId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
