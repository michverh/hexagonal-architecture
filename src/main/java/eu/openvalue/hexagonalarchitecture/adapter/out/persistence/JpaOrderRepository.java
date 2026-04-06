package eu.openvalue.hexagonalarchitecture.adapter.out.persistence;

import eu.openvalue.hexagonalarchitecture.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaOrderRepository extends JpaRepository<JpaOrderEntity, Long> {

    List<JpaOrderEntity> findByStatus(OrderStatus status);
}
