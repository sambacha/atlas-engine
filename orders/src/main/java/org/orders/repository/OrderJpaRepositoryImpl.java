package org.atlas.engine.financialexchange.orders.repository;

import java.util.List;

import org.atlas.engine.financialexchange.orders.domain.Order;
import org.atlas.engine.financialexchange.orders.domain.OrderImpl;
import org.atlas.engine.financialexchange.orders.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("orderJpaRepositoryImpl")
public class OrderJpaRepositoryImpl implements OrderRepository {

	@Autowired
	OrderJpaRepository orderJpaRepository;
	
	@Override
	public long saveOrder(OrderImpl order) {
		long id = -1;
		try {
			Order savedOrder = orderJpaRepository.saveAndFlush(order);
			id = savedOrder.getId();
 		} catch (Exception e) {
			e.printStackTrace();
		}
		return id;
	}

	@Override
	public boolean deleteOrder(long orderId) {
		boolean successfullyDeleted = false;
		try {
			orderJpaRepository.deleteById(orderId);
			successfullyDeleted = true;
		} catch (Exception e) {
			e.printStackTrace();
			successfullyDeleted = false;
		}
		return successfullyDeleted;
	}

	@Override
	public Order getOrder(long orderId) {
		return orderJpaRepository.findById(orderId).orElse(null);
	}

	@Override
	public List<Order> getOrdersByProduct(long productId) {
		return orderJpaRepository.findByProductId(productId);
	}

	@Override
	public List<Order> getOrdersByParticipant(long participantId) {
		return orderJpaRepository.findByParticipantId(participantId);
	}

	@Override
	public int getCount() {
		return (int) orderJpaRepository.count();
	}

}
