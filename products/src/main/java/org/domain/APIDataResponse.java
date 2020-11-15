package org.atlas.engine.financialexchange.products.domain;

public class APIDataResponse<T> extends APIResponse {
	
	private T data;
	
	public APIDataResponse() {
	}
	
	public APIDataResponse(T data, boolean success) {
		super(success);
		this.data = data;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
} 
