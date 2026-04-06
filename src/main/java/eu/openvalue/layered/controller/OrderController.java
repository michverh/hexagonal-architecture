package eu.openvalue.layered.controller;

import eu.openvalue.layered.model.Order;
import eu.openvalue.layered.repository.OrderStatus;
import eu.openvalue.layered.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order placeOrder(@RequestBody Order order) {
        return orderService.placeOrder(order);
    }

    @PutMapping("/{id}")
    public Order updateOrder(@PathVariable Long id, @RequestBody Order order) {
        return orderService.updateOrder(id, order);
    }

    @GetMapping
    public List<Order> listOrders(@RequestParam(value = "status", required = false) OrderStatus status) {
        return orderService.listOrders(Optional.ofNullable(status));
    }
}
