package eu.openvalue.layered.controller;

import eu.openvalue.layered.model.Order;
import eu.openvalue.layered.model.OrderStatus;
import eu.openvalue.layered.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order placeOrder(@RequestBody Order order) {
        return orderService.placeOrder(order);
    }

    @PutMapping("/{id}")
    public Order updateOrder(@PathVariable Long id, @RequestBody Order order) {
        return orderService.updateOrder(id, order);
    }

    @PostMapping("/{id}/cancel")
    public Order cancelOrder(@PathVariable Long id) {
        return orderService.cancelOrder(id);
    }

    @PostMapping("/{id}/payments")
    public Order markOrderPaid(@PathVariable Long id) {
        return orderService.markOrderPaid(id);
    }

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @GetMapping
    public List<Order> listOrders(@RequestParam(value = "status", required = false) OrderStatus status) {
        return orderService.listOrders(Optional.ofNullable(status));
    }
}
