package org.atlas.engine.financialexchange.orderbooks.domain;

import java.time.LocalDateTime;

public class OrderActivityEntry {

	private long orderId;
	private OrderActivity orderActivity;
	private LocalDateTime activityTime;
	private int tradedQuantity;
	private int bookedQuantity;
	private int cancelledQuantity;
	
	public int getBookedQuantity() {
		return bookedQuantity;
	}
	public void setBookedQuantity(int bookedQuantity) {
		this.bookedQuantity = bookedQuantity;
	}
	public int getCancelledQuantity() {
		return cancelledQuantity;
	}
	public void setCancelledQuantity(int cancelledQuantity) {
		this.cancelledQuantity = cancelledQuantity;
	}
	public long getOrderId() {
		return orderId;
	}
	public void setOrderId(long orderId) {
		this.orderId = orderId;
	}
	public LocalDateTime getActivityTime() {
		return activityTime;
	}
	public void setActivityTime(LocalDateTime activityTime) {
		this.activityTime = activityTime;
	}
	public int getTradedQuantity() {
		return tradedQuantity;
	}
	public void setTradedQuantity(int tradedQuantity) {
		this.tradedQuantity = tradedQuantity;
	}
	public OrderActivity getOrderActivity() {
		return orderActivity;
	}
	public void setOrderActivity(OrderActivity orderActivity) {
		this.orderActivity = orderActivity;
	}
	
	
}
