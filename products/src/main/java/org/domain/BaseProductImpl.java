package org.atlas.engine.financialexchange.products.domain;

import java.util.concurrent.atomic.AtomicLong;

public abstract class BaseProductImpl implements Product {

	private static AtomicLong idGenerator = new AtomicLong(1);

	private Long id;
	private String symbol;
	private String description;

	public BaseProductImpl() {
		this.id = idGenerator.getAndIncrement();
	}
	
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
	

}
