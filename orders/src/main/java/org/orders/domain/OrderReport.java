package org.atlas.engine.financialexchange.orders.domain;

import java.math.BigDecimal;
import java.util.List;

public class OrderReport {

	private long id;
	private long productId;
	private long participantId;
	private String entryTime;
	private Side side;
	private int originalQuantity;
	private int tradedQuantity;
	private int bookedQuantity;
	private OrderType type;
	private OrderStatus status;
	private OrderLongevity longevity;
	private BigDecimal price;
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	private List<Long> trades;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getProductId() {
		return productId;
	}
	public void setProductId(long productId) {
		this.productId = productId;
	}
	public long getParticipantId() {
		return participantId;
	}
	public void setParticipantId(long participantId) {
		this.participantId = participantId;
	}
	public String getEntryTime() {
		return entryTime;
	}
	public void setEntryTime(String entryTime) {
		this.entryTime = entryTime;
	}
	public Side getSide() {
		return side;
	}
	public void setSide(Side side) {
		this.side = side;
	}
	public int getOriginalQuantity() {
		return originalQuantity;
	}
	public void setOriginalQuantity(int originalQuantity) {
		this.originalQuantity = originalQuantity;
	}
	public int getTradedQuantity() {
		return tradedQuantity;
	}
	public void setTradedQuantity(int tradedQuantity) {
		this.tradedQuantity = tradedQuantity;
	}
	public int getBookedQuantity() {
		return bookedQuantity;
	}
	public void setBookedQuantity(int bookedQuantity) {
		this.bookedQuantity = bookedQuantity;
	}
	public OrderType getType() {
		return type;
	}
	public void setType(OrderType type) {
		this.type = type;
	}
	public OrderStatus getStatus() {
		return status;
	}
	public void setStatus(OrderStatus status) {
		this.status = status;
	}
	public OrderLongevity getLongevity() {
		return longevity;
	}
	public void setLongevity(OrderLongevity longevity) {
		this.longevity = longevity;
	}
	public List<Long> getTrades() {
		return trades;
	}
	public void setTrades(List<Long> trades) {
		this.trades = trades;
	}
}
