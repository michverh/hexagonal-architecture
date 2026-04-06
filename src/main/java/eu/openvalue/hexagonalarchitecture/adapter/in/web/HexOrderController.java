package eu.openvalue.hexagonalarchitecture.adapter.in.web;

import eu.openvalue.hexagonalarchitecture.application.port.in.*;
import eu.openvalue.hexagonalarchitecture.domain.Order;
import eu.openvalue.hexagonalarchitecture.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/hex/orders")
@RequiredArgsConstructor
public class HexOrderController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final UpdateOrderUseCase updateOrderUseCase;
    private final ListOrdersUseCase listOrdersUseCase;
    private final HexOrderMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse placeOrder(@RequestBody HexOrderRequest request) {
        Order order = placeOrderUseCase.placeOrder(mapper.toPlaceOrderCommand(request));
        return mapper.toOrderResponse(order);
    }

    @PutMapping("/{id}")
    public OrderResponse updateOrder(@PathVariable Long id, @RequestBody HexOrderUpdateRequest request) {
        Order order = updateOrderUseCase.updateOrder(mapper.toUpdateOrderCommand(id, request));
        return mapper.toOrderResponse(order);
    }

    @GetMapping
    public List<OrderResponse> listOrders(@RequestParam(value = "status", required = false) OrderStatus status) {
        return listOrdersUseCase.listOrders(Optional.ofNullable(status)).stream()
                .map(mapper::toOrderResponse)
                .toList();
    }

}
