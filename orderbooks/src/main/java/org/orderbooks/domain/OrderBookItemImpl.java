package org.atlas.engine.financialexchange.orderbooks.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderBookItemImpl implements OrderBookItem {

	private long orderId;
	private Side side;
	private int quantity;
	private BigDecimal price;
	private OrderType type;
	private long productId;
	private LocalDateTime entryTime;
	
	@Override
	public long getOrderId() {
		return orderId;
	}
	@Override
	public void setOrderId(long orderId) {
		this.orderId = orderId;
	}
	@Override
	public Side getSide() {
		return side;
	}
	@Override
	public void setSide(Side side) {
		this.side = side;
	}
	@Override
	public int getQuantity() {
		return quantity;
	}
	@Override
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	@Override
	public BigDecimal getPrice() {
		return price;
	}
	@Override
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	@Override
	public OrderType getType() {
		return type;
	}
	@Override
	public void setType(OrderType type) {
		this.type = type;
	}
	@Override
	public long getProductId() {
		return productId;
	}
	@Override
	public void setProductId(long productId) {
		this.productId = productId;
	}
	public LocalDateTime getEntryTime() {
		return entryTime;
	}
	public void setEntryTime(LocalDateTime entryTime) {
		this.entryTime = entryTime;
	}
	public String toString() {
		StringBuffer message = new StringBuffer();
		message.append(" orderId = ").append(this.getOrderId()).append(";");
		message.append(" quantity = ").append(this.getQuantity()).append(";");
		message.append(" side = ").append(this.getSide()).append(";");
		message.append(" qty = ").append(this.getQuantity()).append(";");
		message.append(" price = ").append(this.getPrice());
		return message.toString();
	}

}
