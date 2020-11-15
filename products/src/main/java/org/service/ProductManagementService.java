package org.atlas.engine.financialexchange.products.service;

import org.atlas.engine.financialexchange.products.domain.ExchangeException;
import org.atlas.engine.financialexchange.products.domain.Product;
import org.atlas.engine.financialexchange.products.domain.ProductEntry;

public interface ProductManagementService {
	long addProduct(ProductEntry productEntry) throws ExchangeException;
	Product getProduct(String symbol) throws ExchangeException;
	Product getProduct(long productId) throws ExchangeException;
	void deleteProduct(long productId) throws ExchangeException;
	Product updateProduct(long productId, ProductEntry productEntry) throws ExchangeException;
}

