package org.atlas.engine.financialexchange.trades.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "trade")
public class TradeImpl implements Trade {

	static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss.SSS");

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "trade_time")
	private LocalDateTime tradeTime;
	
	@Column(name = "buy_tradable_id")
	private long buyTradableId;
	
	@Column(name = "sell_tradable_id")
	private long sellTradableId;
	
	@Column(name = "quantity")
	private int quantity;
	
	@Column(name = "price")
	private BigDecimal price;
	
	@Column(name = "busted") 
	private boolean busted;
	
	public boolean isBusted() {
		return busted;
	}

	public void setBusted(boolean busted) {
		this.busted = busted;
	}

	public TradeImpl() {
		this.setTradeTime(LocalDateTime.now());
	}
	
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getTradeTime() {
		return this.tradeTime;
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

	public BigDecimal getPrice() {
		return this.price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public int getQuantity() {
		return this.quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String toString() {
		StringBuffer message = new StringBuffer();
		message.append("-------- Trade ----------\n");
		message.append("ID: ").append(this.getId()).append("\n");
		message.append("Time: " + formatter.format(LocalDateTime.now()) + "\n");
		message.append("Qty: ").append(this.getQuantity()).append("\n");
		message.append("Price: ").append(this.getPrice()).append("\n");
		message.append("Buy Side Tradable Id: ").append(this.getBuyTradableId()).append("  ");
		message.append("Sell Side Tradable Id: ").append(this.getSellTradableId());
		return message.toString();

	}
	
}
