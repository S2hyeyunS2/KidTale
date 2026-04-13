package com.hyeyuns2.kidtale.order.service;

import com.hyeyuns2.kidtale.common.exception.ErrorCode;
import com.hyeyuns2.kidtale.common.exception.KidTaleException;
import com.hyeyuns2.kidtale.external.sweetbook.client.SweetBookClient;
import com.hyeyuns2.kidtale.external.sweetbook.dto.request.CreateOrderRequest;
import com.hyeyuns2.kidtale.external.sweetbook.dto.request.OrderItem;
import com.hyeyuns2.kidtale.external.sweetbook.dto.request.ShippingInfo;
import com.hyeyuns2.kidtale.order.dto.request.OrderCreateRequest;
import com.hyeyuns2.kidtale.order.dto.response.OrderResponse;
import com.hyeyuns2.kidtale.order.entity.Order;
import com.hyeyuns2.kidtale.order.repository.OrderRepository;
import com.hyeyuns2.kidtale.story.entity.Story;
import com.hyeyuns2.kidtale.story.entity.StoryStatus;
import com.hyeyuns2.kidtale.story.repository.StoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final StoryRepository storyRepository;
    private final SweetBookClient sweetBookClient;

    public OrderService(
            OrderRepository orderRepository,
            StoryRepository storyRepository,
            SweetBookClient sweetBookClient
    ) {
        this.orderRepository = orderRepository;
        this.storyRepository = storyRepository;
        this.sweetBookClient = sweetBookClient;
    }

    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        Story story = storyRepository.findById(request.storyId())
                .orElseThrow(() -> new KidTaleException(ErrorCode.STORY_NOT_FOUND));

        validateStoryForOrder(story);

        log.info("[OrderService] 주문 생성 시작. storyId={}, sweetBookId={}",
                story.getId(), story.getSweetBookId());

        // POST /orders 호출
        CreateOrderRequest sweetBookRequest = buildSweetBookOrderRequest(request, story.getSweetBookId());
        String sweetBookOrderId = sweetBookClient.createOrder(sweetBookRequest);
        log.debug("[OrderService] SweetBook 주문 완료. sweetBookOrderId={}", sweetBookOrderId);

        // Order 엔티티 DB 저장
        Order order = Order.create(
                story.getId(),
                sweetBookOrderId,
                request.recipientName(),
                request.recipientPhone(),
                request.postalCode(),
                request.address1(),
                request.address2(),
                request.memo(),
                request.quantity()
        );
        Order saved = orderRepository.save(order);

        // Story 상태 BOOK_CREATED → ORDERED
        story.updateStatusToOrdered(sweetBookOrderId);
        log.info("[OrderService] 주문 생성 완료. orderId={}, storyId={}, sweetBookOrderId={}",
                saved.getId(), story.getId(), sweetBookOrderId);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new KidTaleException(ErrorCode.ORDER_NOT_FOUND));
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse findByStoryId(Long storyId) {
        Order order = orderRepository.findByStoryId(storyId)
                .orElseThrow(() -> new KidTaleException(ErrorCode.ORDER_NOT_FOUND));
        return toResponse(order);
    }

    private void validateStoryForOrder(Story story) {
        if (story.getStatus() == StoryStatus.ORDERED) {
            log.warn("[OrderService] 이미 주문된 동화입니다. storyId={}", story.getId());
            throw new KidTaleException(ErrorCode.ORDER_ALREADY_EXISTS);
        }
        if (story.getStatus() != StoryStatus.BOOK_CREATED) {
            log.warn("[OrderService] 책 생성이 완료되지 않은 동화입니다. storyId={}, status={}",
                    story.getId(), story.getStatus());
            throw new KidTaleException(ErrorCode.ORDER_NOT_BOOK_CREATED);
        }
    }

    private CreateOrderRequest buildSweetBookOrderRequest(OrderCreateRequest request, String sweetBookId) {
        OrderItem item = new OrderItem(sweetBookId, request.quantity());
        ShippingInfo shipping = new ShippingInfo(
                request.recipientName(),
                request.recipientPhone(),
                request.postalCode(),
                request.address1(),
                request.address2(),
                request.memo()
        );
        return new CreateOrderRequest(List.of(item), shipping, null);
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getStoryId(),
                order.getSweetBookOrderId(),
                order.getRecipientName(),
                order.getRecipientPhone(),
                order.getPostalCode(),
                order.getAddress1(),
                order.getAddress2(),
                order.getMemo(),
                order.getQuantity(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}
