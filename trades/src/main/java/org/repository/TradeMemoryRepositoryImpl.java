package org.atlas.engine.financialexchange.trades.repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.atlas.engine.financialexchange.trades.domain.Trade;
import org.atlas.engine.financialexchange.trades.domain.TradeImpl;
import org.springframework.stereotype.Service;

@Service("tradeMemoryRepositoryImpl")
public class TradeMemoryRepositoryImpl implements TradeRepository {

	private static AtomicLong idGenerator = new AtomicLong(1);
	private Map<Long, Trade> trades; 

	public TradeMemoryRepositoryImpl() {
		trades = new ConcurrentHashMap<Long, Trade>();
	}
	
	public long saveTrade(TradeImpl trade) {
		if (trade.getId() == null) {
			trade.setId(idGenerator.getAndIncrement());
		}
		trades.put(trade.getId(), trade);
		return trade.getId();
	}

	public boolean deleteTrade(long tradeId) {
		Trade trade = trades.remove(tradeId);
		return trade != null;
	}

	public Trade getTrade(long tradeId) {
		return trades.get(tradeId);
	}

	public List<Trade> getTrades(long orderId) {
		List<Trade> tradesForOrder = trades.values().stream()
			.filter(t -> t.getSellTradableId() == orderId || t.getBuyTradableId() == orderId)
			.collect(Collectors.toList());
		return tradesForOrder;
	}
}

