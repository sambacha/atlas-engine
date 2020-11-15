package org.atlas.engine.financialexchange.orders.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class OrderActivityEntry {

	private static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss.SSS");

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
	public String toString() {
		StringBuilder output = new StringBuilder();
		output.append("orderId=").append(orderId).append(";");
		output.append("activity=").append(orderActivity).append(";");
		output.append("time=").append(timeFormatter.format(activityTime)).append(";");
		output.append("traded=").append(tradedQuantity).append(";");
		output.append("booked=").append(bookedQuantity).append(";");
		output.append("cancelled=").append(cancelledQuantity);
		return output.toString();
	}
	
}
