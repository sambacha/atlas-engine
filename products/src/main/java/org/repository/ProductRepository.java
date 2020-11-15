package org.atlas.engine.financialexchange.products.repository;

import java.util.List;

import org.atlas.engine.financialexchange.products.domain.Product;


public interface ProductRepository {
	long saveProduct(Product product);
	boolean deleteProduct(long productId);
	Product getProduct(long productId);
	Product getProduct(String symbol);
	int getCount();
	List<Product> getProducts();
}

