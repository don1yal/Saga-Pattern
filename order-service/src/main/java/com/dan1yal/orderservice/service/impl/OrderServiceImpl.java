package com.dan1yal.orderservice.service.impl;

import com.dan1yal.orderservice.dto.OrderDto;
import com.example.demo.events.order.OrderCreatedEvent;
import com.dan1yal.orderservice.mapper.OrderMapper;
import com.dan1yal.orderservice.model.Order;
import com.dan1yal.orderservice.repository.OrderRepository;
import com.dan1yal.orderservice.request.OrderRequest;
import com.dan1yal.orderservice.service.OrderHistoryService;
import com.dan1yal.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "order")
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderHistoryService orderHistoryService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Value("${orders.event.topic-name}")
    private String orderEventTopicName;

    @Override
    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public OrderDto createOrder(OrderRequest request) {
        Order newOrder = orderMapper.toOrder(request);
        Order savedOrder = orderRepository.save(newOrder);
        orderHistoryService.createOrderHistory(savedOrder.getOrderId());

        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getOrderId(),
                savedOrder.getProductId(),
                savedOrder.getUserId(),
                savedOrder.getQuantity(),
                savedOrder.getPrice()
        );
        kafkaTemplate.send(orderEventTopicName, event);

        return orderMapper.toOrderDto(savedOrder);
    }

    @Override
    @Cacheable(key = "#id", unless = "#result == null")
    public OrderDto getOrderById(String id) {
        return orderRepository.findById(id)
                .map(orderMapper::toOrderDto)
                .orElse(null);
    }

    @Override
    public List<OrderDto> getAllOrders() {
        return orderMapper.toOrderDtoList(orderRepository.findAll());
    }

    @Override
    @CacheEvict(value = {"order", "orders", "history"}, key = "#id")
    public void deleteOrder(String id) {
        if (orderRepository.existsById(id)) {
            orderRepository.deleteById(id);
            orderHistoryService.deleteOrderHistory(id);
        }
    }

    @Override
    @CacheEvict(value = {"order", "orders"}, allEntries = true)
    public void deleteAll() {
        orderRepository.deleteAll();
    }

    @Override
    public void cancelOrder(String orderId) {
        orderHistoryService.updateOrderStatus(orderId.toString(), "CANCELLED");
    }

    @Override
    public void completeOrder(String orderId) {
        orderHistoryService.updateOrderStatus(orderId.toString(), "COMPLETED");
    }
}
