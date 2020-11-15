package org.atlas.engine.financialexchange.products.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.atlas.engine.financialexchange.products.domain.Equity;
import org.atlas.engine.financialexchange.products.domain.Product;
import org.atlas.engine.financialexchange.products.domain.ProductType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("productJpaRepositoryImpl")
public class ProductJpaRepositoryImpl implements ProductRepository {

	@Autowired
	private EquityRepository equityRepository;
	
	@Override
	public long saveProduct(Product product) {
		long id = -1;
		try {
			if (product.getProductType() == ProductType.EQUITY) {
				Equity equity = (Equity) product;
				equityRepository.saveAndFlush(equity);
				Optional<Equity> savedEquity = equityRepository.findBySymbol(product.getSymbol());
				if (savedEquity.isPresent()) {
					id = savedEquity.get().getId();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return id;
	}

	@Override
	public boolean deleteProduct(long productId) {
		boolean successfullyDeleted = false;
		try {
			equityRepository.deleteById(productId);
			successfullyDeleted = true;
		} catch (Exception e) {
			e.printStackTrace();
			successfullyDeleted = false;
		}
		return successfullyDeleted;
	}

	@Override
	public Product getProduct(long productId) {
		Product foundProduct = null;
		try {
			Optional<Equity> equity = equityRepository.findById(productId);
			if (equity.isPresent()) {
				foundProduct = equity.get();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return foundProduct;
	}

	@Override
	public Product getProduct(String symbol) {
		Product foundProduct = null;
		try {
			Optional<Equity> equity = equityRepository.findBySymbol(symbol);
			if (equity.isPresent()) {
				foundProduct = equity.get();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return foundProduct;
	}

	@Override
	public int getCount() {
		int count = (int) equityRepository.count();
		return count;
	}

	@Override
	public List<Product> getProducts() {
		List<Equity> equities = equityRepository.findAll();
		List<Product> products = new ArrayList<Product>();
		equities.forEach(e -> products.add(e));
		return products;
	}

}
