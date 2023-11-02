package com.progilyas.orderservice.service;

import com.progilyas.orderservice.dto.InventoryResponse;
import com.progilyas.orderservice.dto.OrderLineItemsDto;
import com.progilyas.orderservice.dto.OrderRequest;
import com.progilyas.orderservice.model.Order;
import com.progilyas.orderservice.model.OrderLineItems;
import com.progilyas.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    public void placeOrder(OrderRequest orderRequest){
    Order order = new Order();
    order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto).toList();
        order.setOrderLineItems(orderLineItems);



        //collecting all skucodes from client
        List<String> skuCodes =order.getOrderLineItems()
                .stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        //Call inventory service, and place order if product is in stock
        InventoryResponse[] inventoryResponses = webClientBuilder.build().get()
                .uri("http://inventory-service/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode",skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        if (inventoryResponses!=null) {
            boolean allProductsIsInStock = Arrays.stream(inventoryResponses).allMatch(InventoryResponse::isInStock);
            if (allProductsIsInStock ) {
                orderRepository.save(order);
            }else {
                throw  new IllegalArgumentException("Product is not in stock");
            }
        }else {
            throw new IllegalArgumentException("Inventory is empty");}


    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        return orderLineItems;
    }
}
