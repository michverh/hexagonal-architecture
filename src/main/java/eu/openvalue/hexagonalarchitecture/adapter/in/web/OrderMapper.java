package eu.openvalue.hexagonalarchitecture.adapter.in.web;

import eu.openvalue.hexagonalarchitecture.application.port.in.OrderItemCommand;
import eu.openvalue.hexagonalarchitecture.application.port.in.PlaceOrderCommand;
import eu.openvalue.hexagonalarchitecture.application.port.in.UpdateOrderCommand;
import eu.openvalue.hexagonalarchitecture.domain.Order;
import eu.openvalue.hexagonalarchitecture.domain.OrderLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    PlaceOrderCommand toPlaceOrderCommand(OrderRequest request);

    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "customerName", source = "request.customerName")
    @Mapping(target = "shippingAddress", source = "request.shippingAddress")
    @Mapping(target = "items", source = "request.items")
    UpdateOrderCommand toUpdateOrderCommand(Long orderId, OrderUpdateRequest request);

    @Mapping(target = "id", expression = "java(order.id().value())")
    @Mapping(target = "customerEmail", expression = "java(order.customer().email())")
    @Mapping(target = "customerName", expression = "java(order.customer().name())")
    @Mapping(target = "shippingAddress", expression = "java(order.customer().shippingAddress())")
    @Mapping(target = "status", expression = "java(order.status())")
    @Mapping(target = "shippingCost", expression = "java(order.money().shippingCost().asBigDecimal())")
    @Mapping(target = "totalDue", expression = "java(order.money().totalDue().asBigDecimal())")
    @Mapping(target = "items", expression = "java(order.lines().items().stream().map(this::toOrderItemResponse).toList())")
    OrderResponse toOrderResponse(Order order);

    default List<OrderResponse> toOrderResponses(List<Order> orders) {
        return orders == null ? List.of() : orders.stream().map(this::toOrderResponse).toList();
    }

    OrderItemCommand toCommand(OrderItemRequest request);

    @Mapping(target = "unitPrice", expression = "java(line.unitPrice().asBigDecimal())")
    OrderResponse.OrderItemResponse toOrderItemResponse(OrderLine line);
}
