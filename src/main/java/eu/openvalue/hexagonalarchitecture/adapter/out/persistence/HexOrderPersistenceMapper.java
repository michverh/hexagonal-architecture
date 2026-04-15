package eu.openvalue.hexagonalarchitecture.adapter.out.persistence;

import eu.openvalue.hexagonalarchitecture.domain.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HexOrderPersistenceMapper {

    @Mapping(target = "id", expression = "java(order.id().value())")
    @Mapping(target = "customerEmail", expression = "java(order.customer().email())")
    @Mapping(target = "customerName", expression = "java(order.customer().name())")
    @Mapping(target = "shippingAddress", expression = "java(order.customer().shippingAddress())")
    @Mapping(target = "status", expression = "java(order.status())")
    @Mapping(target = "shippingCost", expression = "java(order.money().shippingCost().asBigDecimal())")
    @Mapping(target = "totalDue", expression = "java(order.money().totalDue().asBigDecimal())")
    @Mapping(target = "items", expression = "java(order.lines().items().stream().map(this::toItemEntity).toList())")
    JpaOrderEntity toEntity(Order order);

    default Order toDomain(JpaOrderEntity entity) {
        CustomerInfo customer = new CustomerInfo(
                entity.getCustomerEmail(),
                entity.getCustomerName(),
                entity.getShippingAddress()
        );
        List<OrderLine> lines = entity.getItems().stream()
                .map(this::toDomainLine)
                .toList();
        OrderLines orderLines = OrderLines.of(lines);
        OrderMoneySnapshot money = new OrderMoneySnapshot(
                Money.of(entity.getShippingCost()),
                Money.of(entity.getTotalDue())
        );
        OrderLifecycle lifecycle = OrderLifecycle.restore(entity.getStatus());
        return Order.restore(OrderId.of(entity.getId()), customer, orderLines, money, lifecycle);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "unitPrice", expression = "java(line.unitPrice().asBigDecimal())")
    JpaOrderItemEntity toItemEntity(OrderLine line);

    default OrderLine toDomainLine(JpaOrderItemEntity entity) {
        return new OrderLine(
                entity.getProductCode(),
                entity.getProductName(),
                entity.getQuantity(),
                Money.of(entity.getUnitPrice())
        );
    }
}
