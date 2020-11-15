package org.atlas.engine.financialexchange.trades.repository;

import java.util.List;

import org.atlas.engine.financialexchange.trades.domain.Trade;
import org.atlas.engine.financialexchange.trades.domain.TradeImpl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TradeJpaRepository extends JpaRepository<TradeImpl, Long> {

	@Query("select ti from TradeImpl ti where ti.buyTradableId = :orderId or ti.sellTradableId = :orderId")
	List<Trade> findByOrderId(long orderId);
	
}
