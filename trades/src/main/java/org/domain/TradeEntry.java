package org.atlas.engine.financialexchange.trades.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TradeEntry {

	private Long id;
	private LocalDateTime tradeTime;
	private long buyTradableId;
	private long sellTradableId;
	private int quantity;
	private BigDecimal price;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public LocalDateTime getTradeTime() {
		return tradeTime;
	}
	public void setTradeTime(LocalDateTime tradeTime) {
		this.tradeTime = tradeTime;
	}
	public long getBuyTradableId() {
		return buyTradableId;
	}
	public void setBuyTradableId(long buyTradableId) {
		this.buyTradableId = buyTradableId;
	}
	public long getSellTradableId() {
		return sellTradableId;
	}
	public void setSellTradableId(long sellTradableId) {
		this.sellTradableId = sellTradableId;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	
}
