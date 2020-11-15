package org.atlas.engine.financialexchange.orderbooks.domain;

import java.math.BigDecimal;
import java.util.Map;

/**
 * OrderBookState
 * 
 * @author Hemant
 *
 */
public class OrderBookState {

	private long productId;
	private String productSymbol;
	private Map<BigDecimal, Integer> bids;
	private Map<BigDecimal, Integer> offers;
	
	public long getProductId() {
		return productId;
	}
	public void setProductId(long productId) {
		this.productId = productId;
	}
	public String getProductSymbol() {
		return productSymbol;
	}
	public void setProductSymbol(String productSymbol) {
		this.productSymbol = productSymbol;
	}
	public Map<BigDecimal, Integer> getBids() {
		return bids;
	}
	public void setBids(Map<BigDecimal, Integer> bids) {
		this.bids = bids;
	}
	public Map<BigDecimal, Integer> getOffers() {
		return offers;
	}
	public void setOffers(Map<BigDecimal, Integer> offers) {
		this.offers = offers;
	}
	
	
}
