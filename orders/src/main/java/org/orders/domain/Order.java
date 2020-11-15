package org.atlas.engine.financialexchange.orders.domain;

import java.math.BigDecimal;

public interface Order extends Tradable {

	public int getQuantity();
	public void setQuantity(int quantity);
	public int getTradedQantity();
	public void setTradedQuantity(int tradedQuantity);
	public int getBookedQuantity();
	public void setBookedQuantity(int bookedQuantity);
	public int getCancelledQuantity();
	public void setCancelledQuantity(int cancelledQuantity);
	public BigDecimal getPrice();
	public void setPrice(BigDecimal price);
	public OrderStatus getStatus();
	public void setStatus(OrderStatus orderStatus);
	public OrderType getType();
	public void setType(OrderType type);
	public OrderLongevity getLongevity();
	public void setLongevity(OrderLongevity longevity);
	  
}