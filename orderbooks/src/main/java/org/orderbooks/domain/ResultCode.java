package org.atlas.engine.financialexchange.orderbooks.domain;

public enum ResultCode {

	PRODUCT_ADDED(1001, "Product added"),
	PRODUCT_REJECTED(1002, "Order rejected"), 
	PRODUCT_FOUND(1003, "Product found"),
	PRODUCT_NOT_FOUND(1004, "Product not found"),
	PRODUCT_UPDATED(1005, "Product updated"),
	PRODUCT_DELETED(1006, "Product deleted"),
	PARTICIPANT_ADDED(2001, "Participant added"),
	PARTICIPANT_REJECTED(2002, "Participant rejected"),
	PARTICIPANT_FOUND(2003, "Participant found"),
	PARTICIPANT_NOT_FOUND(2004, "Participant not found"),
	PARTICIPANT_UPDATED(2005, "Participant updated"),
	PARTICIPANT_DELETED(2006, "Participant deleted"),
	ORDER_ACCEPTED(3001, "Order accepted"),
	ORDER_REJECTED(3002, "Order rejected"), 
	ORDER_FOUND(3003, "Order found"),
	ORDER_NOT_FOUND(3004, "Order not found"),
	ORDER_CANCELLED(3005, "Order cancelled"),
	ORDER_FILLED(3006, "Order filled"),
	ORDER_UPDATED(3007, "Order updated"),
	ORDER_BOOK_FOUND(3008, "Order book found"),
	TRADE_ACCEPTED(4001, "Trade accepted"),
	TRADE_REJECTED(4002, "Trade rejected"), 
	TRADE_FOUND(4003, "Trade found"),
	TRADE_NOT_FOUND(4004, "Trade not found"),
	TRADE_BUSTED(4005, "Trade busted"),
	UNSUPPORTED_ENTITY(9000, "Operation on an unsupported entity"),
	GENERAL_ERROR(999999, "General error. Contact Exchange");
	
	ResultCode(int code, String message) {
		this.code = code;
		this.message = message;
	}
	
	private String message;
	private int code;
	
	public int getCode() {
		return this.code;
	}
	
	public String getMessage() {
		return this.message;
	}
}

