package org.atlas.engine.financialexchange.products.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "equity")
public class Equity implements Product, Serializable {

	private static final long serialVersionUID = -3009157732242241606L;

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "symbol")
	private String symbol;
	
	@Column(name = "description")
	private String description;

	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String getSymbol() {
		return this.symbol;
	}

	@Override
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	public String toString() {
		StringBuffer message = new StringBuffer();
		message.append(" id = ").append(this.getId()).append(";");
		message.append(" symbol = ").append(getSymbol());
		return message.toString();
	}

	public ProductType getProductType() {
		return ProductType.EQUITY;
	}
}
