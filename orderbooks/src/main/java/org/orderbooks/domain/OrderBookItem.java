package org.atlas.engine.financialexchange.orderbooks.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface OrderBookItem {

	long getOrderId();

	void setOrderId(long orderId);

	Side getSide();

	void setSide(Side side);

	int getQuantity();

	void setQuantity(int quantity);

	BigDecimal getPrice();

	void setPrice(BigDecimal price);

	OrderType getType();

	void setType(OrderType type);

	long getProductId();

	void setProductId(long productId);
	
	LocalDateTime getEntryTime();
	
	void setEntryTime(LocalDateTime localDateTime);

}