package org.atlas.engine.financialexchange.orders.repository;

import java.util.List;

import org.atlas.engine.financialexchange.orders.domain.Order;
import org.atlas.engine.financialexchange.orders.domain.OrderImpl;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<OrderImpl, Long> {

	public List<Order> findByProductId(long productId);
	public List<Order> findByParticipantId(long participantId);

}
