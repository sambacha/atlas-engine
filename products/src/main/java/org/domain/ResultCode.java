package org.atlas.engine.financialexchange.products.domain;

public enum ResultCode {

	PRODUCT_ADDED(1001, "Product added"),
	PRODUCT_REJECTED(1002, "Order rejected"), 
	PRODUCT_FOUND(1003, "Product found"),
	PRODUCT_NOT_FOUND(1004, "Product not found"),
	PRODUCT_UPDATED(1005, "Product updated"),
	PRODUCT_DELETED(1006, "Product deleted"),
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

