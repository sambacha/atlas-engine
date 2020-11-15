package org.atlas.engine.financialexchange.orders.domain;

public class ExchangeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int errorCode;
	
	public ExchangeException(ResultCode resultCode) {
		this(resultCode.getMessage(), resultCode.getCode());
	}
	
	public ExchangeException(String message, int errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	 
}
