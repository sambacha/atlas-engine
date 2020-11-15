package org.atlas.engine.financialexchange.trades.repository;

import java.util.List;

import org.atlas.engine.financialexchange.trades.domain.Trade;
import org.atlas.engine.financialexchange.trades.domain.TradeImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("tradeJpaRepositoryImpl")
public class TradeJpaRepositoryImpl implements TradeRepository {

	@Autowired
	TradeJpaRepository tradeJpaRepository;

	@Override
	public long saveTrade(TradeImpl trade) {
		long id = -1;
		try {
			Trade savedTrade = tradeJpaRepository.saveAndFlush(trade);
			id = savedTrade.getId();
 		} catch (Exception e) {
			e.printStackTrace();
		}
		return id;
	}

	@Override
	public boolean deleteTrade(long tradeId) {
		boolean successfullyDeleted = false;
		try {
			tradeJpaRepository.deleteById(tradeId);
			successfullyDeleted = true;
		} catch (Exception e) {
			e.printStackTrace();
			successfullyDeleted = false;
		}
		return successfullyDeleted;
	}

	@Override
	public Trade getTrade(long tradeId) {
		return tradeJpaRepository.findById(tradeId).orElse(null);
	}

	@Override
	public List<Trade> getTrades(long orderId) {
		return tradeJpaRepository.findByOrderId(orderId);
	}

	
}
