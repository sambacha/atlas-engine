package org.atlas.engine.financialexchange.trades.service;

import java.util.List;
import java.util.stream.Collectors;

import org.atlas.engine.financialexchange.trades.domain.ExchangeException;
import org.atlas.engine.financialexchange.trades.domain.ResultCode;
import org.atlas.engine.financialexchange.trades.domain.Trade;
import org.atlas.engine.financialexchange.trades.domain.TradeEntry;
import org.atlas.engine.financialexchange.trades.domain.TradeImpl;
import org.atlas.engine.financialexchange.trades.domain.TradeReport;
import org.atlas.engine.financialexchange.trades.repository.TradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("tradeManagementServiceImpl")
public class TradeManagementServiceImpl implements TradeManagementService {
	
	@Autowired
	@Qualifier("tradeJpaRepositoryImpl")
	private TradeRepository tradeRepository;
	
	@Override
	public long acceptTrade(TradeEntry tradeEntry) throws ExchangeException {
		TradeImpl trade = new TradeImpl();
		trade.setBuyTradableId(tradeEntry.getBuyTradableId());
		trade.setPrice(tradeEntry.getPrice());
		trade.setQuantity(tradeEntry.getQuantity());
		trade.setSellTradableId(tradeEntry.getSellTradableId());
		trade.setTradeTime(tradeEntry.getTradeTime());
		long tradeId = tradeRepository.saveTrade(trade);
		if (tradeId < 0) {
			throw new ExchangeException(ResultCode.GENERAL_ERROR);
		}
		return tradeId;
	}

	/**
	 * TODO: Busted trade should result in update to the order and order book
	 */
	@Override
	public void bustTrade(long tradeId) throws ExchangeException {
		Trade trade = tradeRepository.getTrade(tradeId);
		if (trade == null) {
			throw new ExchangeException(ResultCode.TRADE_NOT_FOUND);
		}
		trade.setBusted(true);
	}

	/**
	 * TODO: Trade report should contain descriptive information about the
	 * tradables and not just id.
	 */
	@Override
	public TradeReport getTrade(long tradeId) throws ExchangeException {
		Trade trade = tradeRepository.getTrade(tradeId);
		if (trade == null) {
			throw new ExchangeException(ResultCode.TRADE_NOT_FOUND);
		}
		return getTradeReport(trade);
	}

	@Override
	public List<TradeReport> getTradesForOrder(long orderId) throws ExchangeException {
		List<Trade> trades = tradeRepository.getTrades(orderId);
		if (trades.size() == 0) {
			throw new ExchangeException(ResultCode.TRADE_NOT_FOUND);
		}
		List<TradeReport> tradeReports = trades.stream()
				.map(this::getTradeReport)
				.collect(Collectors.toList());
		return tradeReports;
	}
	
	private TradeReport getTradeReport(Trade trade) {
		TradeReport tradeReport = new TradeReport();
		tradeReport.setId(trade.getId());
		tradeReport.setBuyTradableId(trade.getBuyTradableId());
		tradeReport.setPrice(trade.getPrice());
		tradeReport.setQuantity(trade.getQuantity());
		tradeReport.setSellTradableId(trade.getSellTradableId());
		tradeReport.setTradeTime(trade.getTradeTime());
		
		return tradeReport;
	}
}
