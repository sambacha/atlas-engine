package org.atlas.engine.financialexchange.orderbooks.service;

import org.atlas.engine.financialexchange.orderbooks.domain.ExchangeException;
import org.atlas.engine.financialexchange.orderbooks.domain.OrderBookEntry;
import org.atlas.engine.financialexchange.orderbooks.domain.OrderBookState;

public interface OrderBookService {
	OrderBook getOrderBook(long productId) throws ExchangeException;
	void deleteOrderBook(long productId);
	void addOrder(OrderBookEntry orderBookEntry) throws ExchangeException;
	void cancelOrder(long productId, long orderId) throws ExchangeException;
	OrderBookState getOrderBookMontage(long productId);
}

