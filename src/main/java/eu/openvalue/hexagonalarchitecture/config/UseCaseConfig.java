package eu.openvalue.hexagonalarchitecture.config;

import eu.openvalue.hexagonalarchitecture.adapter.out.persistence.OrderPersistenceAdapter;
import eu.openvalue.hexagonalarchitecture.application.service.ListOrdersService;
import eu.openvalue.hexagonalarchitecture.application.service.PlaceOrderService;
import eu.openvalue.hexagonalarchitecture.application.service.UpdateOrderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

  @Bean
  public ListOrdersService listOrdersService(OrderPersistenceAdapter orderPersistenceAdapter) {
    return new ListOrdersService(orderPersistenceAdapter);
  }

  @Bean
  public PlaceOrderService placeOrderService(OrderPersistenceAdapter orderPersistenceAdapter) {
    return new PlaceOrderService(orderPersistenceAdapter);
  }

  @Bean
  public UpdateOrderService updateOrderService(OrderPersistenceAdapter orderPersistenceAdapter) {
    return new UpdateOrderService(orderPersistenceAdapter, orderPersistenceAdapter);
  }
}
