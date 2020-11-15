package org.atlas.engine.financialexchange.trades.domain;

public enum ResultCode {

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

