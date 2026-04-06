package eu.openvalue.hexagonalarchitecture.application.service;

import eu.openvalue.hexagonalarchitecture.application.port.in.ListOrdersUseCase;
import eu.openvalue.hexagonalarchitecture.application.port.out.OrderLookupPort;
import eu.openvalue.hexagonalarchitecture.domain.Order;
import eu.openvalue.hexagonalarchitecture.domain.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ListOrdersService implements ListOrdersUseCase {

    private final OrderLookupPort lookupPort;

    public ListOrdersService(OrderLookupPort lookupPort) {
        this.lookupPort = lookupPort;
    }

    @Override
    public List<Order> listOrders(Optional<OrderStatus> statusFilter) {
        return statusFilter.map(lookupPort::findByStatus)
                .orElseGet(lookupPort::findAll);
    }
}
