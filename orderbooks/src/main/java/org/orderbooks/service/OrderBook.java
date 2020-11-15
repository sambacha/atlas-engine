package org.atlas.engine.financialexchange.orderbooks.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import org.atlas.engine.financialexchange.orderbooks.domain.OrderBookItem;
import org.atlas.engine.financialexchange.orderbooks.domain.Side;

public interface OrderBook {
	void processOrder(OrderBookItem order);

	void cancelOrder(long orderId);

	void modifyOrder(int qId, HashMap<String, String> order);

	int getVolumeAtPrice(Side side, BigDecimal price);

	BigDecimal getBestBid();

	BigDecimal getWorstBid();

	BigDecimal getBestOffer();

	BigDecimal getWorstOffer();

	int volumeOnSide(Side side);

	BigDecimal getTickSize();

	BigDecimal getSpread();

	BigDecimal getMid();

	boolean bidsAndAsksExist();

	String toString();

	long getProductId();

	void setProductId(long productId);
	
	String getProductSymbol();
	
	void setProductSymbol(String productSymbol);

	void setOrderBookItems(List<OrderBookItem> items);


}

