package org.atlas.engine.financialexchange.products.domain;

public interface Product {
	public Long getId();
	public void setId(Long id);
	public String getSymbol();
	public void setSymbol(String symbol);
	public String getDescription();
	public void setDescription(String description);
	public ProductType getProductType();

}

