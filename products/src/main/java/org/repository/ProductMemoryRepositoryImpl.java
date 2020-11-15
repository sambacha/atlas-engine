package org.atlas.engine.financialexchange.products.repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.atlas.engine.financialexchange.products.domain.Product;
import org.springframework.stereotype.Service;

@Service("productMemoryRepositoryImpl")
public class ProductMemoryRepositoryImpl implements ProductRepository {

	private static AtomicLong idGenerator = new AtomicLong(1);

	Map<Long, Product> products; 
	
	public ProductMemoryRepositoryImpl() {
		products = new ConcurrentHashMap<Long, Product>();
	}
	
	@Override
	public long saveProduct(Product product) {
		if (product.getId() == null) {
			product.setId(idGenerator.getAndIncrement());
		}
		products.put(product.getId(), product);
		return product.getId();
	}

	@Override
	public boolean deleteProduct(long productId) {
		Product product = products.remove(productId);
		return product != null;
	}

	@Override
	public Product getProduct(long productId) {
		return products.get(productId);
	}

	@Override
	public int getCount() {
		return products.size();
	}

	@Override
	public Product getProduct(String symbol) {
		Product product = products.values().stream()
			.filter(p -> p.getSymbol().equals(symbol))
			.findFirst().orElse(null);
		return product;
	}

	@Override
	public List<Product> getProducts() {
		List<Product> productSet = products.values().stream().collect(Collectors.toList());
		return productSet;
	}

}

